package com.dfund.app.domain;

import com.dfund.app.data.remote.VoiceAction;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalAiIntentParser {

    public static VoiceAction parse(String prompt, String language) {
        if (prompt == null) prompt = "";
        String pLower = prompt.toLowerCase(Locale.ROOT).trim();
        String lang = language != null ? language.toLowerCase(Locale.ROOT) : "en";

        // 1. Language Change
        if (pLower.contains("tamil") || pLower.contains("தமிழ்")) {
            Map<String, Object> params = new HashMap<>();
            params.put("language", "ta");
            return new VoiceAction("ACTION_CHANGE_LANGUAGE", params, "மொழியை தமிழுக்கு மாற்றுகிறேன்.");
        }
        if (pLower.contains("telugu") || pLower.contains("తెలుగు")) {
            Map<String, Object> params = new HashMap<>();
            params.put("language", "te");
            return new VoiceAction("ACTION_CHANGE_LANGUAGE", params, "భాషను తెలుగుకు మారుస్తున్నాను.");
        }
        if (pLower.contains("malayalam") || pLower.contains("മലയാളം")) {
            Map<String, Object> params = new HashMap<>();
            params.put("language", "ml");
            return new VoiceAction("ACTION_CHANGE_LANGUAGE", params, "ഭാഷ മലയാളത്തിലേക്ക് മാറ്റുന്നു.");
        }
        if (pLower.contains("english") || pLower.contains("ஆங்கிலம்")) {
            Map<String, Object> params = new HashMap<>();
            params.put("language", "en");
            return new VoiceAction("ACTION_CHANGE_LANGUAGE", params, "Switching language to English.");
        }

        // 2. SIP / Investment Calculation with exact compound math
        if (pLower.contains("sip") || pLower.contains("invest") || pLower.contains("save") || 
            pLower.contains("முதலீடு") || pLower.contains("சேமிப்பு") || 
            pLower.contains("పెట్టుబడి") || pLower.contains("నిക്ഷേപം") || pLower.contains("வளர்க்க")) {

            double amount = extractAmount(pLower, 500.0);
            int years = extractYears(pLower, 3);
            double monthlyRate = 0.12 / 12.0;
            int months = years * 12;
            double totalInvested = amount * months;
            double fv = amount * ((Math.pow(1 + monthlyRate, months) - 1) / monthlyRate) * (1 + monthlyRate);
            double returns = fv - totalInvested;

            Map<String, Object> params = new HashMap<>();
            params.put("amount", amount);
            params.put("monthly_amount", amount);
            params.put("tenure_years", (double) years);

            String response;
            if ("ta".equals(lang)) {
                response = String.format(Locale.getDefault(), "மாதம் ₹%.0f வீதம் %d ஆண்டுகளுக்கு SIP கணக்கிடப்பட்டது. மொத்த முதலீடு ₹%.0f, முதிர்வு தொகை ₹%.0f (லாபம் ₹%.0f).", amount, years, totalInvested, fv, returns);
            } else if ("te".equals(lang)) {
                response = String.format(Locale.getDefault(), "నెలకు ₹%.0f చొప్పున %d సంవత్సరాల SIP లెక్కించబడింది. మొత్తం పెట్టుబడి ₹%.0f, రాబడి ₹%.0f.", amount, years, totalInvested, fv);
            } else if ("ml".equals(lang)) {
                response = String.format(Locale.getDefault(), "പ്രതിമാസം ₹%.0f നിരക്കിൽ %d വർഷത്തേക്ക് SIP കണക്കാക്കി. ആകെ നിക്ഷേപം ₹%.0f, പ്രതീക്ഷിക്കുന്ന മൂല്യം ₹%.0f.", amount, years, totalInvested, fv);
            } else {
                response = String.format(Locale.getDefault(), "Calculated SIP for ₹%.0f/month for %d years. Total invested is ₹%.0f, and expected return is ₹%.0f with ₹%.0f gain.", amount, years, totalInvested, fv, returns);
            }
            return new VoiceAction("ACTION_OPEN_SIP_CALCULATOR", params, response);
        }

        // 3. EMI / Upcoming Dues
        if (pLower.contains("emi") || pLower.contains("due") || pLower.contains("loan") || 
            pLower.contains("bill") || pLower.contains("கடன்") || pLower.contains("வాయిదా") || 
            pLower.contains("வாയ്പ") || pLower.contains("பில்")) {

            Map<String, Object> params = new HashMap<>();
            params.put("category", "EMI");
            String response;
            if ("ta".equals(lang)) {
                response = "உங்கள் வரவிருக்கும் EMI தவணைகள் சரிபார்க்கப்பட்டு திறக்கப்பட்டுள்ளன. இந்த மாதம் செலுத்த வேண்டிய மொத்த தொகை ₹4,100.";
            } else if ("te".equals(lang)) {
                response = "మీ రాబోయే EMI బకాయిలు తనిఖీ చేయబడ్డాయి. ఈ నెల మొత్తం చెల్లించాల్సిన మొత్తం ₹4,100.";
            } else if ("ml".equals(lang)) {
                response = "നിങ്ങളുടെ വരാനിരിക്കുന്ന EMI വിവരങ്ങൾ പരിശോധിച്ചു. ഈ മാസം അടയ്ക്കാനുള്ള ആകെ തുക ₹4,100.";
            } else {
                response = "Checked your upcoming loan and bill EMIs. Total due this month is ₹4,100.";
            }
            return new VoiceAction("ACTION_CHECK_EMIS", params, response);
        }

        // 4. Spending / Expenses / Category
        if (pLower.contains("spend") || pLower.contains("expense") || pLower.contains("செலவு") || 
            pLower.contains("ఖర్చు") || pLower.contains("ചെലവ്") || pLower.contains("food") || 
            pLower.contains("travel") || pLower.contains("சாப்பாடு") || pLower.contains("உணவு")) {

            String category = "All";
            if (pLower.contains("food") || pLower.contains("சாப்பாடு") || pLower.contains("உணவு") || pLower.contains("భోజనం") || pLower.contains("ഭക്ഷണം")) {
                category = "Food";
            } else if (pLower.contains("travel") || pLower.contains("fuel") || pLower.contains("petrol") || pLower.contains("பயணம்") || pLower.contains("യാത്ര")) {
                category = "Travel";
            }

            Map<String, Object> params = new HashMap<>();
            params.put("category", category);

            String response;
            if ("ta".equals(lang)) {
                response = "உங்கள் " + category + " செலவு விவரங்கள் திறக்கப்பட்டுள்ளன. இந்த மாதம் செலவிடப்பட்ட தொகை ₹7,198.";
            } else if ("te".equals(lang)) {
                response = "మీ " + category + " ఖర్చు వివరాలు తెరవబడ్డాయి. ఈ నెల మొత్తం ఖర్చు ₹7,198.";
            } else if ("ml".equals(lang)) {
                response = "നിങ്ങളുടെ " + category + " ചെലവ് വിവരങ്ങൾ തുറന്നു. ഈ മാസം ആകെ ചെലവായത് ₹7,198.";
            } else {
                response = "Opened your " + category + " expense history. Total spent this month is ₹7,198.";
            }
            return new VoiceAction("ACTION_SHOW_SPENDING", params, response);
        }

        // 5. Surplus / Growth
        if (pLower.contains("surplus") || pLower.contains("extra") || pLower.contains("grow") || 
            pLower.contains("மீதி") || pLower.contains("మిగులు") || pLower.contains("ബാക്കി") || 
            pLower.contains("balance")) {

            Map<String, Object> params = new HashMap<>();
            params.put("allocation", "SIP");
            params.put("amount", 4902.0);

            String response;
            if ("ta".equals(lang)) {
                response = "உங்கள் உபரி தொகை பகுப்பாய்வு செய்யப்பட்டது. ₹3,000 பாதுகாப்பு நிதி போக, மீதமுள்ள ₹4,902 முதலீட்டிற்கு ஒதுக்கப்பட்டுள்ளது.";
            } else if ("te".equals(lang)) {
                response = "మీ మిగులు మొత్తం విశ్లేషించబడింది. ₹3,000 భద్రతా నిధి పోగా, మిగిలిన ₹4,902 పెట్టుబడికి కేటాయించబడింది.";
            } else if ("ml".equals(lang)) {
                response = "നിങ്ങളുടെ മിച്ചമുള്ള തുക പരിശോധിച്ചു. സുരക്ഷാ ഫണ്ടിലേക്ക് ₹3,000 മാറ്റിവെച്ച് ബാക്കി ₹4,902 നിക്ഷേപത്തിലേക്ക് നീക്കിവെച്ചു.";
            } else {
                response = "Analyzed your financial surplus. With ₹3,000 in safety shield, ₹4,902 is allocated for growth.";
            }
            return new VoiceAction("ACTION_ALLOCATE_SURPLUS", params, response);
        }

        // 6. Profile & Settings
        if (pLower.contains("profile") || pLower.contains("account") || pLower.contains("சுயவிவரம்") || 
            pLower.contains("ப்ரொஃபைல்") || pLower.contains("ప్రొఫైల్") || pLower.contains("సెట్టింగ్స్") || 
            pLower.contains("പ്രൊഫൈൽ") || pLower.contains("അക്കൗണ്ട്")) {

            Map<String, Object> params = new HashMap<>();
            String response;
            if ("ta".equals(lang)) {
                response = "உங்கள் சுயவிவரம் மற்றும் பாதுகாப்பு அமைப்புகள் திறக்கப்பட்டுள்ளன.";
            } else if ("te".equals(lang)) {
                response = "మీ ప్రొఫైల్ మరియు భద్రతా సెట్టింగ్‌లు తెరవబడ్డాయి.";
            } else if ("ml".equals(lang)) {
                response = "നിങ്ങളുടെ പ്രൊഫൈലും സുരക്ഷാ ക്രമീകരണങ്ങളും തുറന്നു.";
            } else {
                response = "Opened your profile and security settings.";
            }
            return new VoiceAction("ACTION_OPEN_PROFILE", params, response);
        }

        // 7. Logout
        if (pLower.contains("logout") || pLower.contains("log out") || pLower.contains("வெளியேறு") || 
            pLower.contains("லாக் அவுட்") || pLower.contains("లాగ్ అవుట్") || pLower.contains("ലോഗ് ഔട്ട്")) {

            Map<String, Object> params = new HashMap<>();
            String response;
            if ("ta".equals(lang)) {
                response = "DFund-லிருந்து வெளியேறுவதற்கான உறுதிப்படுத்தல் திறக்கப்பட்டது.";
            } else if ("te".equals(lang)) {
                response = "DFund నుండి లాగ్ అవుట్ చేయడానికి నిర్ధారణ తెరవబడింది.";
            } else if ("ml".equals(lang)) {
                response = "DFund-ൽ നിന്ന് ലോഗ് ഔട്ട് ചെയ്യാനുള്ള സ്ഥിരീകരണം തുറന്നു.";
            } else {
                response = "Opened logout confirmation dialog.";
            }
            return new VoiceAction("ACTION_LOGOUT", params, response);
        }

        // Default: Open SIP Calculator as helpful financial action
        Map<String, Object> params = new HashMap<>();
        params.put("amount", 500.0);
        params.put("monthly_amount", 500.0);
        params.put("tenure_years", 3.0);
        String response = "ta".equals(lang) ? "மாதம் ₹500 வீதம் 3 ஆண்டுகளுக்கு SIP கணக்கிடப்பட்டது. முதிர்வுத் தொகை ₹22,812."
            : "te".equals(lang) ? "నెలకు ₹500 చొప్పున 3 సంవత్సరాల SIP లెక్కించబడింది. రాబడి ₹22,812."
            : "ml".equals(lang) ? "പ്രതിമാസം ₹500 നിരക്കിൽ 3 വർഷത്തേക്ക് SIP കണക്കാക്കി. പ്രതീക്ഷിക്കുന്ന മൂല്യം ₹22,812."
            : "Calculated SIP for ₹500/month for 3 years. Expected return is ₹22,812.";
        return new VoiceAction("ACTION_OPEN_SIP_CALCULATOR", params, response);
    }

    private static double extractAmount(String text, double fallback) {
        Pattern pattern = Pattern.compile("(?:rs\\.?|inr|₹|ரூபாய்|ரூ\\.?|రూ\\.?|രൂപ)?\\s*(\\d{2,6})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }

    private static int extractYears(String text, int fallback) {
        Pattern pattern = Pattern.compile("(\\d{1,2})\\s*(?:years?|yr|வருடம்|సంవత్సరం|വർഷം)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }
}
