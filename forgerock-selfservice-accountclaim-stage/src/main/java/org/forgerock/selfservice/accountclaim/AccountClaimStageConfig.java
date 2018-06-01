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

import org.forgerock.selfservice.core.config.StageConfig;

import java.util.Objects;

/**
 * Represents the config for a Twilio stage.
 *
 */
public final class AccountClaimStageConfig implements StageConfig {

    private final static String NAME = "AccountClaim";

    @Override
    public String getProgressStageClassName() {
        return AccountClaimStage.class.getName();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getProgressStageClassName());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AccountClaimStageConfig)) {
            return false;
        }

        AccountClaimStageConfig that = (AccountClaimStageConfig) o;
        return Objects.equals(getName(), that.getName())
                && Objects.equals(getProgressStageClassName(), that.getProgressStageClassName());
    }
}
