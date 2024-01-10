package com.api.calculator.stockprice.brokerage.extractor.extractor;

import com.api.calculator.stockprice.api.persistence.model.ExtractionError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.api.calculator.stockprice.StringUtils.stringToFloat;

public class LiquidValue {

    private final Pattern LIQUID_LINE_PATTERN;
    private final Pattern LIQUID_VALUE_PATTERN;

    private final List<ExtractionError> extractionErrors;

    private final UUID fileId;

    public LiquidValue(UUID fileId) {

        this.fileId = fileId;
        extractionErrors = new ArrayList<>();
        LIQUID_LINE_PATTERN = Pattern.compile("((.*)L ?í?i?quido para [0-9]{2}/[0-9]{2}/[0-9]{4} *[C-D])",Pattern.CASE_INSENSITIVE);
        LIQUID_VALUE_PATTERN = Pattern.compile("(([0-9]*[.])*[0-9]+,[0-9]{2})");
    }

    public List<ExtractionError> getExtractionErrors() {
        return extractionErrors;
    }

    public float liquidValueFoundInLine(Matcher liquidValueMatcher, int multiplier) {
        liquidValueMatcher = LIQUID_VALUE_PATTERN.matcher(liquidValueMatcher.group());
        return stringToFloat(liquidValueMatcher.group()) * multiplier;
    }

    public Matcher getMatcher(float sumValue, String contentString) {

        int digits = String.valueOf((int) sumValue).length()-1;
        return Pattern.compile("(([0-9]*[.]*[0-9]+){" + digits + "},[0-9]{2} *["
                + (sumValue > 0 ? "C" : "D") + "])").matcher(contentString);
    }

    public List<Float> findAllPossibilities(float sumValue, String contentString) {

        Matcher liquidValueMatcher = getMatcher(sumValue, contentString);
        List<Float> liquidValues = new ArrayList<>();

        while (liquidValueMatcher.find()) {
            try {
                String valueString = liquidValueMatcher.group();
                liquidValues.add(stringToFloat(valueString.substring(0, valueString.length() - 1)));

            } catch (Exception ignored) {}
        }
        return liquidValues;
    }

    public float getWithoutShape(float sumValue, int multiplier, String contentString) {

        List<Float> liquidValues = findAllPossibilities(sumValue, contentString);

        if (liquidValues.isEmpty()) {
            extractionErrors.add(new ExtractionError(null, "Valor líquido não definido.", fileId));
            return sumValue;
        }

        return Collections.min(liquidValues)*multiplier;
    }

    public float getValue(float sumValue, String lastBovespaSplitStr) {

        int multiplier = sumValue > 0 ? 1 : -1;

        Matcher liquidValueMatcher = LIQUID_LINE_PATTERN.matcher(lastBovespaSplitStr);
        if (liquidValueMatcher.find()){
            return liquidValueFoundInLine(liquidValueMatcher, multiplier);
        } else {
            return getWithoutShape(sumValue, multiplier, lastBovespaSplitStr);
        }
    }
}
