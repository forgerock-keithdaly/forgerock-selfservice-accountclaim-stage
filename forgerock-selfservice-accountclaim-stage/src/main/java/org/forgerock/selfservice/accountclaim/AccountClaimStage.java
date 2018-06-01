/* 
* 
* Copyright © 2017 ForgeRock, AS. All Rights Reserved.
*
* The code is provided on an "as is" basis, 
* without warranty of any kind, 
* to the fullest extent permitted by law. 
*
* ForgeRock does not warrant or guarantee the individual success 
* developers may have in implementing the code on their 
* development platforms or in production configurations.
*
* ForgeRock does not warrant, guarantee or make any representations 
* regarding the use, results of use, accuracy, timeliness or completeness 
* of any data or information relating to the alpha release of unsupported code. 
* ForgeRock disclaims all warranties, expressed or implied, and in particular, 
* disclaims all warranties of merchantability, and warranties related to the code, 
* or any service or software related thereto.
* 
* ForgeRock shall not be liable for any direct, indirect or consequential 
* damages or costs of any type arising out of any action taken by you 
* or others related to the code.
* 
*/

package org.forgerock.selfservice.accountclaim;

import javax.inject.Inject;

import com.sun.org.apache.regexp.internal.RE;
import org.forgerock.json.JsonValue;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.ConnectionFactory;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.selfservice.core.IllegalStageTagException;
import org.forgerock.selfservice.core.ProcessContext;
import org.forgerock.selfservice.core.ProgressStage;
import org.forgerock.selfservice.core.StageResponse;
import org.forgerock.selfservice.core.util.RequirementsBuilder;
import org.forgerock.selfservice.core.annotations.SelfService;

import static org.forgerock.selfservice.core.ServiceUtils.INITIAL_TAG;

import static org.forgerock.json.JsonValue.object;

/**
 * Progress stages prompts for the Account Claim
 *
 */
public final class AccountClaimStage implements ProgressStage<AccountClaimStageConfig> {

    private static final String VALIDATE_ACCOUNT_CLAIM_TAG = "validateAccountClaim";

    static String ACCOUNT_NUMBER_FIELD = "accountNumber";
    static String ZIP_CODE_FIELD = "zipCode";

    private final ConnectionFactory connectionFactory;

    /**
     * Constructs a new Account Claim stage.
     *
     * @param connectionFactory
     *         the CREST connection factory
     */
    @Inject
    public AccountClaimStage(@SelfService ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
	
    @Override
    public JsonValue gatherInitialRequirements(
            ProcessContext context, AccountClaimStageConfig config) throws ResourceException {

        // gatherInitialRequirements() is only invoked if there are no querystring parameters

        // The user is new, so we do not perform any account claiming and move directly to the next step.
        return RequirementsBuilder.newEmptyRequirements();

    }

    @Override
    public StageResponse advance(
            ProcessContext context, AccountClaimStageConfig config) throws ResourceException {

        switch (context.getStageTag()) {
            case INITIAL_TAG:
                return showFields(context, config);
            case VALIDATE_ACCOUNT_CLAIM_TAG:
                return validateFields(context, config);
        }

        throw new IllegalStageTagException(context.getStageTag());
    }

    private StageResponse showFields(
            ProcessContext context, AccountClaimStageConfig config) throws ResourceException {

        JsonValue customerType = context.getInput().get("customerType");
        JsonValue requirements = null;

        if (customerType.isNull()) {
            return StageResponse
                    .newBuilder()
                    .build();
        }

        switch (customerType.asString()) {
            case "residential":
                context.putState("customerType", "residential");
                requirements = RequirementsBuilder
                        .newInstance("Account Information")
                        .addProperty("policyNumber", "string", "Policy Number")
                        .addProperty("last6ssn", "string", "Last 6 digits of your SSN")
                        .build();
                break;
            case "agent":
                context.putState("customerType", "agent");
                requirements = RequirementsBuilder
                        .newInstance("Account Information")
                        .addProperty("accessCode", "string", "Access Code")
                        .addProperty("zipCode", "string", "Zip Code")
                        .build();
                break;
            default:
                return StageResponse
                        .newBuilder()
                        .build();
        }

        return StageResponse.newBuilder()
                .setStageTag(VALIDATE_ACCOUNT_CLAIM_TAG)
                .setRequirements(requirements)
                //.setCallback(callback)
                .build();
    }

    private StageResponse validateFields(
            ProcessContext context, AccountClaimStageConfig config) throws ResourceException {

        // Residential Customer (residential) --> accountNumber + last6ssn
        // Agent Customer (agent) --> accessCode + zipCode

        //JsonValue customerType = context.getInput().get("customerType");
        JsonValue customerType = context.getState("customerType");

        switch (customerType.asString()) {

            case "residential":

                JsonValue policyNumber = context.getInput().get("policyNumber");
                JsonValue last6ssn = context.getInput().get("last6ssn");

                if (policyNumber.isNull()) {
                    throw new BadRequestException("Required account number is missing");
                }

                if (last6ssn.isNull()) {
                    throw new BadRequestException("Required last 6 digits of your SSN is missing");
                }

                int policyNumberValue;
                int last6ssnValue;

                try {
                    policyNumberValue = Integer.parseInt(policyNumber.asString());
                } catch (NumberFormatException nfE) {
                    throw new BadRequestException("Supplied account number is not a number", nfE);
                }

                try {
                    last6ssnValue = Integer.parseInt(last6ssn.asString());
                } catch (NumberFormatException nfE) {
                    throw new BadRequestException("Supplied zip code is not a number", nfE);
                }

                if (898989 != last6ssnValue) {
                    throw new BadRequestException("Supplied SSN is wrong");
                }

                if (321321 != policyNumberValue) {
                    throw new BadRequestException("Supplied account number is wrong");
                }

                return StageResponse
                        .newBuilder()
                        .build();

            case "agent":

                JsonValue accessCode = context.getInput().get("accessCode");
                JsonValue zipCode = context.getInput().get("zipCode");

                if (accessCode.isNull()) {
                    throw new BadRequestException("Required account number is missing");
                }

                if (zipCode.isNull()) {
                    throw new BadRequestException("Required last 6 digits of your SSN is missing");
                }

                int accessCodeValue;
                int zipCodeValue;

                try {
                    accessCodeValue = Integer.parseInt(accessCode.asString());
                } catch (NumberFormatException nfE) {
                    throw new BadRequestException("Supplied access code is not a number", nfE);
                }

                try {
                    zipCodeValue = Integer.parseInt(zipCode.asString());
                } catch (NumberFormatException nfE) {
                    throw new BadRequestException("Supplied zip code is not a number", nfE);
                }

                if (123456 != accessCodeValue) {
                    throw new BadRequestException("Supplied access code is wrong");
                }

                if (11233 != zipCodeValue) {
                    throw new BadRequestException("Supplied zip code is wrong");
                }

                return StageResponse
                        .newBuilder()
                        .build();

            default:

                return StageResponse
                        .newBuilder()
                        .build();

        }
    }
}
