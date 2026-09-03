import asyncio
import sys
import os

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from httpx import AsyncClient, ASGITransport
from app.main import app
from app.database import init_db

async def run_all_tests():
    print("==================================================")
    print("  RUNNING FASTAPI BACKEND & SARVAM AI TESTS")
    print("==================================================")
    
    await init_db()
    passed = 0
    failed = 0
    transport = ASGITransport(app=app)

    async with AsyncClient(transport=transport, base_url="http://test") as client:
        # Test 1: Health Check
        try:
            res = await client.get("/api/health")
            assert res.status_code == 200, f"Status code was {res.status_code}"
            data = res.json()
            assert data["status"] == "healthy"
            print(f"✅ [PASS] Health Check Endpoint ({data['service']} v{data['version']})")
            passed += 1
        except Exception as e:
            print(f"❌ [FAIL] Health Check Endpoint: {e}")
            failed += 1

        # Test 2: SIP Calculation
        try:
            res = await client.get("/api/savings/calculate-sip?monthly_amount=1000&annual_rate=12&tenure_years=3")
            assert res.status_code == 200
            data = res.json()
            assert data["invested_amount"] == 36000.0
            assert data["total_value"] > 43000.0
            print(f"✅ [PASS] SIP Calculator API (Invested: ₹{data['invested_amount']} -> Total Value: ₹{data['total_value']})")
            passed += 1
        except Exception as e:
            print(f"❌ [FAIL] SIP Calculator API: {e}")
            failed += 1

        # Test 3: Small Surplus Comparison (Idle Bank vs Micro-SIP vs Gold)
        try:
            res = await client.get("/api/savings/compare-surplus?amount=2000&tenure_years=3")
            assert res.status_code == 200
            data = res.json()
            assert data["idle_bank_value"] < data["micro_sip_value"]
            print(f"✅ [PASS] Surplus Comparison API (Idle Bank: ₹{data['idle_bank_value']} vs Micro-SIP: ₹{data['micro_sip_value']})")
            passed += 1
        except Exception as e:
            print(f"❌ [FAIL] Surplus Comparison API: {e}")
            failed += 1

        # Test 4: Voice Interaction (English) - Open SIP Calculator Intent
        try:
            payload = {
                "text_prompt": "Calculate SIP for 500 rupees for 3 years",
                "device_id": "test_device_001",
                "preferred_language": "en"
            }
            res = await client.post("/api/voice/interact", data=payload)
            assert res.status_code == 200
            data = res.json()
            action = data["action"]
            assert action["action_type"] == "ACTION_OPEN_SIP_CALCULATOR"
            assert action["parameters"]["amount"] == 500.0
            assert action["parameters"]["tenure_years"] == 3
            print(f"✅ [PASS] Voice Action Dispatch (English: \"{payload['text_prompt']}\" -> {action['action_type']})")
            passed += 1
        except Exception as e:
            print(f"❌ [FAIL] Voice Action Dispatch English: {e}")
            failed += 1

        # Test 5: Voice Interaction (Tamil) - Switch Language & Reason Intent
        try:
            payload = {
                "text_prompt": "Tamilil pesu",
                "device_id": "test_device_001",
                "preferred_language": "ta"
            }
            res = await client.post("/api/voice/interact", data=payload)
            assert res.status_code == 200
            data = res.json()
            action = data["action"]
            assert action["action_type"] == "ACTION_CHANGE_LANGUAGE"
            assert action["parameters"]["language"] == "ta"
            print(f"✅ [PASS] Voice Action Dispatch (Tamil: \"{payload['text_prompt']}\" -> {action['action_type']} '{action['spoken_response'][:30]}...')")
            passed += 1
        except Exception as e:
            print(f"❌ [FAIL] Voice Action Dispatch Tamil: {e}")
            failed += 1

        # Test 6: Voice Interaction (Food Expenses Query)
        try:
            payload = {
                "text_prompt": "Show my spending on food this month",
                "device_id": "test_device_001",
                "preferred_language": "en"
            }
            res = await client.post("/api/voice/interact", data=payload)
            assert res.status_code == 200
            data = res.json()
            action = data["action"]
            assert action["action_type"] == "ACTION_SHOW_SPENDING"
            assert action["parameters"]["category"] == "FOOD"
            print(f"✅ [PASS] Voice Action Dispatch (Category Query: \"{payload['text_prompt']}\" -> {action['action_type']})")
            passed += 1
        except Exception as e:
            print(f"❌ [FAIL] Voice Action Dispatch Category Query: {e}")
            failed += 1

        # Test 7: Transaction Ingestion & Summary Calculation
        try:
            # Create credit
            await client.post("/api/transactions/", json={
                "device_id": "test_device_001",
                "bank_name": "State Bank of India",
                "amount": 3500.0,
                "type": "CREDIT",
                "category": "SALARY"
            })
            # Create EMI debit
            await client.post("/api/transactions/", json={
                "device_id": "test_device_001",
                "bank_name": "HDFC Bank",
                "amount": 1000.0,
                "type": "DEBIT",
                "category": "EMI",
                "is_emi": True
            })
            # Create food debit
            await client.post("/api/transactions/", json={
                "device_id": "test_device_001",
                "bank_name": "State Bank of India",
                "amount": 500.0,
                "type": "DEBIT",
                "category": "FOOD"
            })

            # Check summary
            sum_res = await client.get("/api/transactions/summary?device_id=test_device_001")
            assert sum_res.status_code == 200
            summary = sum_res.json()
            assert summary["total_income"] == 3500.0
            assert summary["total_spending"] == 1500.0
            assert summary["total_emi"] == 1000.0
            assert summary["available_surplus"] == 2000.0
            assert summary["safety_shield_balance"] == 1000.0
            assert summary["growth_pot_balance"] == 1000.0
            print(f"✅ [PASS] Financial Overview & Summary (Income: ₹{summary['total_income']}, Spending: ₹{summary['total_spending']}, Surplus: ₹{summary['available_surplus']})")
            passed += 1
        except Exception as e:
            print(f"❌ [FAIL] Financial Overview & Summary: {e}")
            failed += 1

    print("==================================================")
    print(f"  FASTAPI SUMMARY: {passed} PASSED, {failed} FAILED")
    print("==================================================")
    
    if failed > 0:
        sys.exit(1)

if __name__ == "__main__":
    asyncio.run(run_all_tests())
