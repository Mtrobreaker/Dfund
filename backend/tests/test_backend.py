import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app

@pytest.mark.asyncio
async def test_health_check():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        response = await client.get("/api/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "healthy"
        assert "DFund" in data["service"]

@pytest.mark.asyncio
async def test_sip_calculation():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        response = await client.get("/api/savings/calculate-sip?monthly_amount=1000&annual_rate=12&tenure_years=3")
        assert response.status_code == 200
        data = response.json()
        assert data["invested_amount"] == 36000.0
        assert data["total_value"] > 43000.0

@pytest.mark.asyncio
async def test_surplus_comparison():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        response = await client.get("/api/savings/compare-surplus?amount=2000&tenure_years=3")
        assert response.status_code == 200
        data = response.json()
        assert data["idle_bank_value"] < data["micro_sip_value"]

@pytest.mark.asyncio
async def test_voice_interaction_intent_english():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        payload = {
            "text_prompt": "Calculate SIP for 500 rupees for 3 years",
            "device_id": "test_device_1",
            "preferred_language": "en"
        }
        response = await client.post("/api/voice/interact", data=payload)
        assert response.status_code == 200
        data = response.json()
        assert data["action"]["action_type"] == "ACTION_OPEN_SIP_CALCULATOR"
        assert data["action"]["parameters"]["amount"] == 500.0
        assert data["action"]["parameters"]["tenure_years"] == 3
        assert "tamil_translation" in data
        assert len(data["tamil_translation"]) > 0
        assert "financial_suggestion" in data
        assert len(data["financial_suggestion"]) > 0
        assert "audio_base64" in data

@pytest.mark.asyncio
async def test_voice_interaction_intent_tamil():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        payload = {
            "text_prompt": "Tamilil pesu",
            "device_id": "test_device_1",
            "preferred_language": "ta"
        }
        response = await client.post("/api/voice/interact", data=payload)
        assert response.status_code == 200
        data = response.json()
        assert data["action"]["action_type"] == "ACTION_CHANGE_LANGUAGE"
        assert data["action"]["parameters"]["language"] == "ta"
        assert "tamil_translation" in data

@pytest.mark.asyncio
async def test_batch_transaction_sync():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        import time
        dev_id = f"sync_device_{int(time.time()*1000)}"
        payload = [
            {
                "device_id": dev_id,
                "bank_name": "HDFC",
                "amount": 1200.0,
                "type": "DEBIT",
                "category": "FOOD",
                "vpa": "swiggy@hdfc",
                "raw_message": f"Rs. 1200.00 debited from HDFC for Swiggy #{dev_id}",
                "is_recurring": False,
                "is_emi": False
            },
            {
                "device_id": dev_id,
                "bank_name": "SBI",
                "amount": 3500.0,
                "type": "DEBIT",
                "category": "EMI",
                "vpa": "sbi.loan@upi",
                "raw_message": f"Rs. 3500.00 debited for Loan EMI #{dev_id}",
                "is_recurring": True,
                "is_emi": True
            }
        ]
        response = await client.post("/api/transactions/sync", json=payload)
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "success"
        assert data["imported"] >= 1

