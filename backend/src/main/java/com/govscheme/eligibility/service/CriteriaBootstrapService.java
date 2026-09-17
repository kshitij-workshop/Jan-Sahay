package com.govscheme.eligibility.service;

import com.govscheme.eligibility.entity.SchemeEligibility;
import com.govscheme.eligibility.entity.SchemeEligibilityRepository;
import com.govscheme.scheme.entity.Scheme;
import com.govscheme.scheme.entity.SchemeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Derives machine-readable criteria from unambiguous source signals only.
 * Today that is a single conservative rule: a scheme whose own name, ministry
 * or department explicitly scopes it to Bihar gets {@code states=Bihar}.
 * Everything else is left uncurated (evaluating to INSUFFICIENT_INFORMATION)
 * rather than guessed at — inventing requirements is worse than admitting
 * ignorance. Existing criteria rows are never overwritten.
 */
@Service
public class CriteriaBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(CriteriaBootstrapService.class);
    private static final Pattern BIHAR = Pattern.compile("\\bbihar\\b|बिहार", Pattern.CASE_INSENSITIVE);

    private final SchemeRepository schemeRepository;
    private final SchemeEligibilityRepository eligibilityRepository;

    public CriteriaBootstrapService(SchemeRepository schemeRepository,
                                    SchemeEligibilityRepository eligibilityRepository) {
        this.schemeRepository = schemeRepository;
        this.eligibilityRepository = eligibilityRepository;
    }

    public record BootstrapSummary(int scanned, int curated, int alreadyCurated, int skipped) {
    }

    @Transactional
    public BootstrapSummary bootstrap() {
        List<Scheme> schemes = schemeRepository.findAll();
        int curated = 0;
        int alreadyCurated = 0;
        for (Scheme scheme : schemes) {
            if (eligibilityRepository.existsById(scheme.getId())) {
                alreadyCurated++;
                continue;
            }
            if (mentionsBihar(scheme)) {
                SchemeEligibility criteria = new SchemeEligibility();
                criteria.setSchemeId(scheme.getId());
                criteria.setStates("Bihar");
                eligibilityRepository.save(criteria);
                curated++;
            }
        }
        BootstrapSummary summary =
            new BootstrapSummary(schemes.size(), curated, alreadyCurated, schemes.size() - curated - alreadyCurated);
        log.info("CURATION_BOOTSTRAPPED scanned={} curated={} alreadyCurated={} skipped={}",
            summary.scanned(), summary.curated(), summary.alreadyCurated(), summary.skipped());
        return summary;
    }

    private boolean mentionsBihar(Scheme scheme) {
        return matches(scheme.getSchemeName())
            || matches(scheme.getNodalMinistry())
            || matches(scheme.getDepartment());
    }

    private boolean matches(String value) {
        return value != null && BIHAR.matcher(value).find();
    }
}
