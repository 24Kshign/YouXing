
package com.share.jack.demoutils;

import android.content.Context;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;

import com.share.jack.swingtravel.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SmileUtils {
    public static final String ee_1 = ":blush:";
    public static final String ee_2 = ":yum:";
    public static final String ee_3 = ":relieved:";
    public static final String ee_4 = ":heart_eyes:";
    public static final String ee_5 = ":sunglasses:";
    public static final String ee_6 = ":smirk:";
    public static final String ee_7 = ":kissing_closed_eyes:";
    public static final String ee_8 = ":stuck_out_tongue:";
    public static final String ee_9 = ":stuck_out_tongue_winking_eye:";
    public static final String ee_10 = ":stuck_out_tongue_closed_eyes:";
    public static final String ee_11 = ":disappointed:";
    public static final String ee_12 = ":worried:";
    public static final String ee_13 = ":sleepy:";
    public static final String ee_14 = ":weary:";
    public static final String ee_15 = ":grimacing:";
    public static final String ee_16 = ":sob:";
    public static final String ee_17 = ":open_mouth:";
    public static final String ee_18 = ":hushed:";
    public static final String ee_19 = ":grinning:";
    public static final String ee_20 = ":grin:";
    public static final String ee_21 = ":joy:";
    public static final String ee_22 = ":smiley:";
    public static final String ee_23 = ":smile:";
    public static final String ee_24 = ":sweat_smile:";
    public static final String ee_25 = ":satisfied:";
    public static final String ee_26 = ":innocent:";
    public static final String ee_27 = ":smiling_imp:";
    public static final String ee_28 = ":wink:";
    public static final String ee_29 = ":neutral_face:";
    public static final String ee_30 = ":expressionless:";
    public static final String ee_31 = ":unamused:";
    public static final String ee_32 = ":sweat:";
    public static final String ee_33 = ":pensive:";
    public static final String ee_34 = ":confused:";
    public static final String ee_35 = ":confounded:";
    public static final String ee_36 = ":kissing:";
    public static final String ee_37 = ":kissing_heart:";
    public static final String ee_38 = ":kissing_smiling_eyes:";
    public static final String ee_39 = ":angry:";
    public static final String ee_40 = ":rage:";
    public static final String ee_41 = ":cry:";
    public static final String ee_42 = ":persevere:";
    public static final String ee_43 = ":triumph:";
    public static final String ee_44 = ":disappointed_relieved:";
    public static final String ee_45 = ":frowning:";
    public static final String ee_46 = ":anguished:";
    public static final String ee_47 = ":fearful:";
    public static final String ee_48 = ":weary:";
    public static final String ee_49 = ":cold_sweat:";
    public static final String ee_50 = ":scream:";
    public static final String ee_51 = ":astonished:";
    public static final String ee_52 = ":flushed:";
    public static final String ee_53 = ":sleeping:";
    public static final String ee_54 = ":dizzy_face:";
    public static final String ee_55 = ":mask:";
    public static final String ee_56 = ":smiley_cat:";
    public static final String ee_57 = ":heart_eyes_cat:";
    public static final String ee_58 = ":smirk_cat:";
    public static final String ee_59 = ":kissing_cat:";
    public static final String ee_60 = ":pouting_cat:";
    public static final String ee_61 = ":crying_cat_face:";
    public static final String ee_62 = ":speak_no_evil:";
    public static final String ee_63 = ":smile_cat:";
    public static final String ee_64 = ":joy_cat:";
    public static final String ee_65 = ":scream_cat:";


    private static final Factory spannableFactory = Factory.getInstance();

    private static final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();

    static {
        addPattern(emoticons, ee_1, R.drawable.ee_1);
        addPattern(emoticons, ee_2, R.drawable.ee_2);
        addPattern(emoticons, ee_3, R.drawable.ee_3);
        addPattern(emoticons, ee_4, R.drawable.ee_4);
        addPattern(emoticons, ee_5, R.drawable.ee_5);
        addPattern(emoticons, ee_6, R.drawable.ee_6);
        addPattern(emoticons, ee_7, R.drawable.ee_7);
        addPattern(emoticons, ee_8, R.drawable.ee_8);
        addPattern(emoticons, ee_9, R.drawable.ee_9);
        addPattern(emoticons, ee_10, R.drawable.ee_10);
        addPattern(emoticons, ee_11, R.drawable.ee_11);
        addPattern(emoticons, ee_12, R.drawable.ee_12);
        addPattern(emoticons, ee_13, R.drawable.ee_13);
        addPattern(emoticons, ee_14, R.drawable.ee_14);
        addPattern(emoticons, ee_15, R.drawable.ee_15);
        addPattern(emoticons, ee_16, R.drawable.ee_16);
        addPattern(emoticons, ee_17, R.drawable.ee_17);
        addPattern(emoticons, ee_18, R.drawable.ee_18);
        addPattern(emoticons, ee_19, R.drawable.ee_19);
        addPattern(emoticons, ee_20, R.drawable.ee_20);
        addPattern(emoticons, ee_21, R.drawable.ee_21);
        addPattern(emoticons, ee_22, R.drawable.ee_22);
        addPattern(emoticons, ee_23, R.drawable.ee_23);
        addPattern(emoticons, ee_24, R.drawable.ee_24);
        addPattern(emoticons, ee_25, R.drawable.ee_25);
        addPattern(emoticons, ee_26, R.drawable.ee_26);
        addPattern(emoticons, ee_27, R.drawable.ee_27);
        addPattern(emoticons, ee_28, R.drawable.ee_28);
        addPattern(emoticons, ee_29, R.drawable.ee_29);
        addPattern(emoticons, ee_30, R.drawable.ee_30);
        addPattern(emoticons, ee_31, R.drawable.ee_31);
        addPattern(emoticons, ee_32, R.drawable.ee_32);
        addPattern(emoticons, ee_33, R.drawable.ee_33);
        addPattern(emoticons, ee_34, R.drawable.ee_34);
        addPattern(emoticons, ee_35, R.drawable.ee_35);

        addPattern(emoticons, ee_36, R.drawable.ee_36);
        addPattern(emoticons, ee_37, R.drawable.ee_37);
        addPattern(emoticons, ee_38, R.drawable.ee_38);
        addPattern(emoticons, ee_39, R.drawable.ee_39);
        addPattern(emoticons, ee_40, R.drawable.ee_40);
        addPattern(emoticons, ee_41, R.drawable.ee_41);
        addPattern(emoticons, ee_42, R.drawable.ee_42);
        addPattern(emoticons, ee_43, R.drawable.ee_43);
        addPattern(emoticons, ee_44, R.drawable.ee_44);
        addPattern(emoticons, ee_45, R.drawable.ee_45);
        addPattern(emoticons, ee_46, R.drawable.ee_46);
        addPattern(emoticons, ee_47, R.drawable.ee_47);
        addPattern(emoticons, ee_48, R.drawable.ee_48);
        addPattern(emoticons, ee_49, R.drawable.ee_49);
        addPattern(emoticons, ee_50, R.drawable.ee_50);
        addPattern(emoticons, ee_51, R.drawable.ee_51);
        addPattern(emoticons, ee_52, R.drawable.ee_52);
        addPattern(emoticons, ee_53, R.drawable.ee_53);
        addPattern(emoticons, ee_54, R.drawable.ee_54);
        addPattern(emoticons, ee_55, R.drawable.ee_55);
        addPattern(emoticons, ee_56, R.drawable.ee_56);
        addPattern(emoticons, ee_57, R.drawable.ee_57);
        addPattern(emoticons, ee_58, R.drawable.ee_58);
        addPattern(emoticons, ee_59, R.drawable.ee_59);
        addPattern(emoticons, ee_60, R.drawable.ee_60);
        addPattern(emoticons, ee_61, R.drawable.ee_61);
        addPattern(emoticons, ee_62, R.drawable.ee_62);
        addPattern(emoticons, ee_63, R.drawable.ee_63);
        addPattern(emoticons, ee_64, R.drawable.ee_64);
        addPattern(emoticons, ee_65, R.drawable.ee_65);

    }

    private static void addPattern(Map<Pattern, Integer> map, String smile,int resource) {
        map.put(Pattern.compile(Pattern.quote(smile)), resource);
    }

    /**
     * replace existing spannable with smiles
     *
     * @param context
     * @param spannable
     * @return
     */
    public static boolean addSmiles(Context context, Spannable spannable) {
        boolean hasChanges = false;
        for (Entry<Pattern, Integer> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(),
                        matcher.end(), ImageSpan.class))
                    if (spannable.getSpanStart(span) >= matcher.start()
                            && spannable.getSpanEnd(span) <= matcher.end())
                        spannable.removeSpan(span);
                    else {
                        set = false;
                        break;
                    }
                if (set) {
                    hasChanges = true;
                    spannable.setSpan(new ImageSpan(context, entry.getValue()),
                            matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return hasChanges;
    }

    public static Spannable getSmiledText(Context context, CharSequence text) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmiles(context, spannable);
        return spannable;
    }

    public static boolean containsKey(String key) {
        boolean b = false;
        for (Entry<Pattern, Integer> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(key);
            if (matcher.find()) {
                b = true;
                break;
            }
        }
        return b;
    }
}
