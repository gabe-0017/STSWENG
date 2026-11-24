package ph.civiccare.domain.registration;

/**
 * Disability category recorded on a PWD registration. This is sensitive health-related data:
 * it is shown only to the registrant and LGU staff, and is never put on-chain or returned by
 * public ID verification.
 */
public enum DisabilityClassification {
    DEAF_OR_HARD_OF_HEARING,
    INTELLECTUAL,
    LEARNING,
    MENTAL,
    PHYSICAL,
    PSYCHOSOCIAL,
    SPEECH_AND_LANGUAGE,
    VISUAL,
    CANCER,
    RARE_DISEASE
}
