package com.company.claimx.constants;

public class ErrorMessageConstants {

    public static final String INVALID_CREDIENTIALS = "Email or Password invalid";

    public static final String USER_NOT_FOUND = "USER not found";
    public static final String USER_INACTIVE = "USER is inactive";
    public static final String USER_NOT_FOUND_WITH_ID = "USER not found with id:";
    public static final String AUTHENTICATION_FAILED = "Authentication Failed";

    public static final String USER_NOT_FOUND_WITH_EMAIL = "USER not found with email:";
    public static final String EMPLOYEE_NOT_FOUND = "EMPLOYEE not found";
    public static final String EMPLOYEE_MANAGER_NOT_FOUND = "EMPLOYEE'S MANAGER not found";

    public static final String MANAGER_NOT_FOUND = "MANAGER not found with email:";
    public static final String FINANCE_USER_NOT_FOUND = "FINANCE USER not found with email : ";
    public static final String ADMIN_NOT_FOUND = "ADMIN USER not found";
    public static final String UNAUTHORIZED_ACCESS = "USER is not authorized to access";
    public static final String UNAUTHORIZED_CLAIM_SUBMISSION = "You can only submit your own claims";
    public static final String UNAUTHORIZED_CLAIM_UPDATING = "You can only update your own claims";
    public static final String UNAUTHORIZED_CLAIM_DELETION = "You can only delete your own claims";
    public static final String UNAUTHORIZED_CLAIM_APPROVAL = "You can only approve your own claims";


    public static final String UNAUTHORIZED_ITEM_DELETION = "You can only delete your own claim's items";
    public static final String UNAUTHORIZED_ITEM_UPDATE = "You can only update your own claim's items";


    public static final String UNAUTHORIZED_EXPENSE_ITEM_ADDITION = "You can only add items to your own claim";
    public static final String INVALID_CLAIM_STATUS_FOR_ITEM_ADDITION = "The status must be DRAFT inorder to add the items, Current Status:";


    public static final String INVALID_CLAIM_STATUS_FOR_CLAIM_SUBMISSION = "Submit allowed only when in DRAFT state, current state:";
    public static final String INVALID_CLAIM_STATUS_FOR_CLAIM_DELETION = "Delete allowed only when in DRAFT state, current state:";
    public static final String INVALID_CLAIM_STATUS_FOR_CLAIM_UPDATE = "Update allowed only when in DRAFT state, current state:";
    public static final String INVALID_CLAIM_STATUS_FOR_CLAIM_PAY = "CLAIM can be paid only when the status is in APPROVED state, current state:";
    public static final String INVALID_CLAIM_STATUS_FOR_CLAIM_APPROVAL = "Approve or Reject is allowed only when in claim is in SUBMITTED state, current state:";


    public static final String INVALID_CLAIM_STATUS_EMPTY_EXPENSE_ITEMS = "Cannot submit the claim without expense item";
    public static final String INVALID_CLAIM_STATUS_FOR_ITEM_DELETION = "Delete allowed only when in DRAFT state, current state:";
    public static final String INVALID_CLAIM_STATUS_FOR_ITEM_UPDATE = "Update allowed only when in DRAFT state, current state:";


    public static final String CLAIM_NOT_FOUND = "Claim not found";
    public static final String CLAIM_NOT_FOUND_WITH_ID = "No claim found with id: ";
    public static final String ITEM_NOT_FOUND_WITH_ID = "Item not found with the id: ";

    public static final String ITEM_DOES_NOT_BELONG_TO_CLAIM = "Item does not belong to this claim : ";


    public static final String USER_NOT_AUTHENTICATED = "User is not authenticated";
    public static final String USER_NOT_AUTHENTICATED_NO_EMAIL_FOUND = "Invalid authentication, no email found";
    public static final String ROLES_REQUIRED_ACCESS_DENIED = "Access denied for user, Required roles: ";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String ROLE = "ROLE_";


}
