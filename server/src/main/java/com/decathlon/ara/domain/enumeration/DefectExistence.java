package com.decathlon.ara.domain.enumeration;

/**
 * When a defect (frm an external defect tracking system) is assigned to a problem, its presence is checked.<br>
 * This is the result of that check.
 */
public enum DefectExistence {

    /**
     * The defect tracking system was not responding when creating/updating the problem while assigning a new defect ID:
     * defect status will get queried again on next indexing.
     */
    UNKNOWN,

    /**
     * The defect ID assigned to a problem has been found in the defect tracking system.
     */
    EXISTS,

    /**
     * The defect ID assigned to a problem has NOT been found in the defect tracking system.<br>
     * This should forbid problem creation or update, but if the defect tracking system cannot be contacted in such
     * case, creation/update is still validated, and defect presence is validated asynchronously: nonexistence is then
     * reported later.
     */
    NONEXISTENT

}
