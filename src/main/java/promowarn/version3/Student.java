package promowarn.version3;

import java.util.Optional;
import promowarn.common.mail.EMailAddress;

public interface Student {
    Integer id();

    EMailAddress email();

    Optional<Double> grade();

    void grade(final double grade);
}
