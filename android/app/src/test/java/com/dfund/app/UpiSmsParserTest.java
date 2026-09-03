package com.dfund.app;

import com.dfund.app.data.local.TransactionEntity;
import com.dfund.app.data.sms.UpiSmsParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class UpiSmsParserTest {

    @Test
    public void testParseSbiCredit() {
        String sender = "SBIINB";
        String message = "Your A/C *1234 is credited with Rs 3,200.00 on 01-Sep via UPI from swiggy.partner@sbi Ref: SBI90234812.";
        TransactionEntity tx = UpiSmsParser.parse(sender, message, System.currentTimeMillis());

        assertNotNull("Should parse valid banking SMS", tx);
        assertEquals("CREDIT", tx.getType());
        assertEquals(3200.0, tx.getAmount(), 0.01);
        assertEquals("swiggy.partner@sbi", tx.getVpa());
        assertEquals("SBI90234812", tx.getUtr());
        assertEquals("State Bank of India", tx.getBankName());
    }

    @Test
    public void testParseHdfcDebitFood() {
        String sender = "HDFCBK";
        String message = "Rs 180.00 debited from HDFC Bank A/C *5678 on 02-Sep to SWIGGY UPI: swiggy@icici. Ref: HDF12987345";
        TransactionEntity tx = UpiSmsParser.parse(sender, message, System.currentTimeMillis());

        assertNotNull(tx);
        assertEquals("DEBIT", tx.getType());
        assertEquals(180.0, tx.getAmount(), 0.01);
        assertEquals("FOOD", tx.getCategory());
        assertEquals("HDFC Bank", tx.getBankName());
    }

    @Test
    public void testParseEmiMandate() {
        String sender = "AXISBK";
        String message = "Auto-debit for Two-Wheeler Loan EMI of Rs 1,250.00 debited from A/C *9876 on 03-Sep. Mandate Ref: AXS98123456";
        TransactionEntity tx = UpiSmsParser.parse(sender, message, System.currentTimeMillis());

        assertNotNull(tx);
        assertEquals("DEBIT", tx.getType());
        assertEquals(1250.0, tx.getAmount(), 0.01);
        assertEquals("EMI", tx.getCategory());
        assertTrue("isEmi flag should be true", tx.isEmi());
    }

    @Test
    public void testIgnoreSpamOrNonMonetarySms() {
        String sender = "DM-PROMO";
        String message = "Get 50% discount on your next ride with code SAVE50. Valid till midnight!";
        TransactionEntity tx = UpiSmsParser.parse(sender, message, System.currentTimeMillis());

        assertNull("Should ignore non-monetary promotional SMS", tx);
    }
}
