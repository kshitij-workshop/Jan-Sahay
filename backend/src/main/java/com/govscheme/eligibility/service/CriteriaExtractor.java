package com.govscheme.eligibility.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.govscheme.eligibility.entity.SchemeEligibility;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mines machine-readable criteria from published scheme text using
 * conservative, deterministic patterns — never an LLM, never a guess.
 * <p>
 * Every pattern is deliberately narrow and every dimension documents its
 * traps:
 * <ul>
 *   <li>age is only read from applicant-scoped sentences (a "deceased earning
 *       member should be above 18" must never become an applicant age rule);</li>
 *   <li>income is only set from a single unambiguous cap — area-conditional
 *       caps (rural vs urban) are skipped, not averaged;</li>
 *   <li>gender is skipped when both genders or transgender inclusion appear;</li>
 *   <li>caste lists are ignored when the text reads open ("all categories");</li>
 *   <li>occupations require eligible/for/applicant phrasing, not background mentions.</li>
 * </ul>
 * Absence of a match leaves the field {@code null} (unconstrained), which the
 * engine treats as unknown rather than inventing a requirement.
 */
public final class CriteriaExtractor {

    private CriteriaExtractor() {
    }

    private static final String APPLICANT =
        "(?:applicant|beneficiar(?:y|ies)|candidate|representative|person|individual)";

    private static final Pattern AGE_BETWEEN = Pattern.compile(
        APPLICANT + "[^.]{0,80}?aged?\\s+between\\s+(\\d{1,3})\\s+and\\s+(\\d{1,3})",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern AGE_RANGE_DASH = Pattern.compile(
        APPLICANT + "[^.]{0,80}?age\\s+is\\s+between\\s+(\\d{1,3})\\s*[–\\-to]+\\s*(\\d{1,3})",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern AGE_MIN = Pattern.compile(
        APPLICANT + "[^.]{0,80}?(?:must|should) be (?:above|over)\\s+(\\d{1,3})",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern AGE_MAX = Pattern.compile(
        APPLICANT + "[^.]{0,80}?(?:must|should) be (?:below|under)\\s+(\\d{1,3})",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern SENIOR_CITIZEN = Pattern.compile(
        APPLICANT + "[^.]{0,60}?\\bsenior citizens?\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEATH_CONTEXT = Pattern.compile(
        "\\b(?:deceas|death|demise|died)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern INCOME = Pattern.compile(
        "annual(?:\\s+family)?\\s+income[^.]{0,60}?(?:not\\s+exceed(?:ing)?|less\\s+than|below|up\\s+to|maximum|max\\.?)\\s*[₹Rs.\\s]*([\\d,]+(?:\\.\\d+)?)\\s*(lakh|lacs|lac)?",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern FEMALE_TERM =
        Pattern.compile("\\b(?:women|woman|female|girls?|girl child)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MALE_TERM =
        Pattern.compile("\\b(?:men|male|boys?|boy child)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern THIRD_GENDER_TERM =
        Pattern.compile("\\btransgender|third gender\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern STUDENT = Pattern.compile(
        "\\bstudents?\\b.{0,80}?\\b(?:enrolled|studying|study|studies|school|college|universit|institut|bonafide)\\b"
        + "|\\b(?:enrolled|studying|bonafide)\\b.{0,80}?\\bstudents?\\b",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern OPEN_CATEGORY =
        Pattern.compile("\\ball categor|general category[^.]{0,40}?eligible|open to all",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern BPL =
        Pattern.compile("\\bbpl\\b[^.]{0,80}?\\b(?:must|should|belong|eligible|required|hold)\\b"
            + "|\\b(?:must|should)\\b[^.]{0,80}?\\bbpl\\b",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern APL_ALTERNATIVE = Pattern.compile(
        "\\bapl\\b.{0,40}?\\b(?:also|either|or)\\b"
        + "|\\bbpl\\b[^.]{0,30}?\\bor\\b[^.]{0,30}?\\bapl\\b"
        + "|\\bbpl\\b.{0,20}?/\\s*apl\\b",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern DISABILITY = Pattern.compile(
        "\\b(?:persons?\\s+with\\s+disabilit\\w*|divyangjan|pwd|physically challenged|handicapped)[^.]{0,80}?\\b(?:eligible|for|must|should|benefit)\\b"
        + "|\\b(?:eligible|for)\\b[^.]{0,80}?\\b(?:persons?\\s+with\\s+disabilit\\w*|divyangjan)",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern DISABILITY_PCT = Pattern.compile(
        "(\\d{1,3})\\s*%[^.]{0,40}?disab|disab[^.]{0,40}?(\\d{1,3})\\s*%",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern MINORITY = Pattern.compile(
        "\\bminorit(?:y|ies)\\b[^.]{0,80}?\\b(?:communit|eligible|for|must|should)\\b"
        + "|\\b(?:eligible|for)\\b[^.]{0,80}?\\bminorit(?:y|ies)\\b",
        Pattern.CASE_INSENSITIVE);

    private static final List<String> INDIAN_STATES = List.of(
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa",
        "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
        "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland",
        "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
        "Uttar Pradesh", "Uttarakhand", "West Bengal", "Delhi", "Jammu and Kashmir",
        "Ladakh", "Puducherry", "Chandigarh", "Andaman and Nicobar Islands", "Lakshadweep");

    private static final Pattern RESIDENCE = Pattern.compile(
        "(?:permanent\\s+)?resident of\\s+(" + stateAlternatives() + ")"
        + "|domicile of\\s+(" + stateAlternatives() + ")"
        + "|residing(?:\\s+and\\s+working)?\\s+in\\s+(" + stateAlternatives() + ")"
        + "|bonafide resident of\\s+(" + stateAlternatives() + ")",
        Pattern.CASE_INSENSITIVE);

    private static String stateAlternatives() {
        return INDIAN_STATES.stream()
            .sorted((a, b) -> Integer.compare(b.length(), a.length()))
            .map(s -> s.replace(" ", "\\s+"))
            .collect(java.util.stream.Collectors.joining("|"));
    }

    /**
     * Fills a fresh {@link SchemeEligibility} from a file-dump item. Only
     * high-confidence matches set fields; everything else stays {@code null}.
     */
    public static SchemeEligibility extract(JsonNode item) {
        SchemeEligibility criteria = new SchemeEligibility();
        String text = eligibilityText(item);
        if (text.isBlank()) {
            return criteria;
        }
        extractAge(text, criteria);
        extractIncome(text, criteria);
        extractGender(text, criteria);
        extractStudent(text, criteria);
        extractCaste(text, criteria);
        extractOccupation(text, criteria);
        extractResidence(text, criteria);
        extractBpl(text, criteria);
        extractDisability(text, criteria);
        extractMinority(text, criteria);
        return criteria;
    }

    private static String eligibilityText(JsonNode item) {
        StringBuilder sb = new StringBuilder();
        append(sb, item, "eligibility_md");
        append(sb, item, "schemeName");
        return sb.toString();
    }

    private static void append(StringBuilder sb, JsonNode item, String key) {
        JsonNode node = item.get(key);
        if (node != null && node.isTextual() && !node.asText().isBlank()) {
            sb.append(node.asText()).append('\n');
        }
    }

    private static void extractAge(String text, SchemeEligibility criteria) {
        Matcher between = AGE_BETWEEN.matcher(text);
        if (between.find() && !inDeathContext(text, between.start())) {
            criteria.setMinAge(Integer.parseInt(between.group(1)));
            criteria.setMaxAge(Integer.parseInt(between.group(2)));
            return;
        }
        Matcher dash = AGE_RANGE_DASH.matcher(text);
        if (dash.find() && !inDeathContext(text, dash.start())) {
            criteria.setMinAge(Integer.parseInt(dash.group(1)));
            criteria.setMaxAge(Integer.parseInt(dash.group(2)));
            return;
        }
        Matcher min = AGE_MIN.matcher(text);
        if (min.find() && !inDeathContext(text, min.start())) {
            criteria.setMinAge(Integer.parseInt(min.group(1)));
        }
        Matcher max = AGE_MAX.matcher(text);
        if (max.find() && !inDeathContext(text, max.start())) {
            criteria.setMaxAge(Integer.parseInt(max.group(1)));
        }
        if (criteria.getMinAge() == null && SENIOR_CITIZEN.matcher(text).find()) {
            criteria.setMinAge(60);
        }
    }

    private static boolean inDeathContext(String text, int pos) {
        int from = Math.max(0, pos - 120);
        return DEATH_CONTEXT.matcher(text.substring(from, pos)).find();
    }

    private static void extractIncome(String text, SchemeEligibility criteria) {
        Matcher matcher = INCOME.matcher(text);
        Set<Long> caps = new LinkedHashSet<>();
        while (matcher.find()) {
            if (inDeathContext(text, matcher.start())) {
                continue;
            }
            try {
                double amount = Double.parseDouble(matcher.group(1).replace(",", ""));
                String unit = matcher.group(2);
                if (unit != null) {
                    amount *= 100000;
                }
                caps.add((long) amount);
            } catch (NumberFormatException ignored) {
            }
        }
        // Multiple distinct caps (e.g. rural vs urban) cannot be expressed as
        // one ceiling — skip rather than pick a wrong one.
        if (caps.size() == 1) {
            criteria.setMaxAnnualIncome(caps.iterator().next());
        }
    }

    private static void extractGender(String text, SchemeEligibility criteria) {
        if (THIRD_GENDER_TERM.matcher(text).find()) {
            return;
        }
        boolean female = FEMALE_TERM.matcher(text).find();
        boolean male = MALE_TERM.matcher(text).find();
        if (female && !male) {
            criteria.setGenders("FEMALE");
        } else if (male && !female) {
            criteria.setGenders("MALE");
        }
    }

    private static void extractStudent(String text, SchemeEligibility criteria) {
        if (STUDENT.matcher(text).find()) {
            criteria.setRequireStudent(true);
        }
    }

    private static void extractCaste(String text, SchemeEligibility criteria) {
        if (OPEN_CATEGORY.matcher(text).find()) {
            return;
        }
        Set<String> castes = new LinkedHashSet<>();
        String lower = text.toLowerCase();
        if (lower.contains("scheduled caste") || Pattern.compile("\\bsc\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()
                || lower.contains("sc/st") || lower.contains("sc & st")) {
            castes.add("SC");
        }
        if (lower.contains("scheduled tribe") || Pattern.compile("\\bst\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            castes.add("ST");
        }
        if (Pattern.compile("\\bobc\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {
            castes.add("OBC");
        }
        if (!castes.isEmpty()) {
            List<String> ordered = new ArrayList<>();
            for (String c : new String[]{"SC", "ST", "OBC"}) {
                if (castes.contains(c)) {
                    ordered.add(c);
                }
            }
            criteria.setCasteCategories(String.join(",", ordered));
        }
    }

    private static void extractOccupation(String text, SchemeEligibility criteria) {
        String lower = text.toLowerCase();
        if (matchesOccupation(lower, "farmer", "farmers", "kisan")) {
            criteria.setOccupations("FARMER");
        } else if (matchesOccupation(lower, "weaver", "weavers")) {
            criteria.setOccupations("WEAVER");
        } else if (matchesOccupation(lower, "artisan", "artisans")) {
            criteria.setOccupations("ARTISAN");
        } else if (matchesOccupation(lower, "fisherman", "fishermen", "fisherfolk", "fisher")) {
            criteria.setOccupations("FISHER");
        }
    }

    private static boolean matchesOccupation(String lower, String... terms) {
        for (String term : terms) {
            int idx = lower.indexOf(term);
            while (idx >= 0) {
                int from = Math.max(0, idx - 80);
                String window = lower.substring(from, idx);
                if (window.contains("for ") || window.contains("eligible") || window.contains("applicant")
                        || window.contains("must be") || window.contains("should be")) {
                    // Exclude background mentions such as "deceased was a farmer".
                    String before = lower.substring(Math.max(0, idx - 30), idx);
                    if (!before.contains("deceased") && !before.contains("death")) {
                        return true;
                    }
                }
                idx = lower.indexOf(term, idx + 1);
            }
        }
        return false;
    }

    private static void extractResidence(String text, SchemeEligibility criteria) {
        Matcher matcher = RESIDENCE.matcher(text);
        Set<String> states = new LinkedHashSet<>();
        while (matcher.find()) {
            for (int g = 1; g <= matcher.groupCount(); g++) {
                String candidate = matcher.group(g);
                if (candidate == null) {
                    continue;
                }
                String state = canonicalState(candidate.trim());
                if (state != null) {
                    states.add(state);
                }
            }
        }
        if (!states.isEmpty()) {
            criteria.setStates(String.join(",", states));
        }
    }

    static String canonicalState(String candidate) {
        for (String state : INDIAN_STATES) {
            if (state.equalsIgnoreCase(candidate)
                    || state.replace(" ", "").equalsIgnoreCase(candidate.replaceAll("\\s+", ""))) {
                return state;
            }
        }
        return null;
    }

    private static void extractBpl(String text, SchemeEligibility criteria) {
        if (APL_ALTERNATIVE.matcher(text).find()) {
            return;
        }
        if (BPL.matcher(text).find()) {
            criteria.setRequireBpl(true);
        }
    }

    private static void extractDisability(String text, SchemeEligibility criteria) {
        if (!DISABILITY.matcher(text).find()) {
            return;
        }
        criteria.setRequireDisabled(true);
        Matcher pct = DISABILITY_PCT.matcher(text);
        if (pct.find()) {
            String value = pct.group(1) != null ? pct.group(1) : pct.group(2);
            try {
                criteria.setMinDisabilityPercentage(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static void extractMinority(String text, SchemeEligibility criteria) {
        if (MINORITY.matcher(text).find()) {
            criteria.setRequireMinority(true);
        }
    }
}
