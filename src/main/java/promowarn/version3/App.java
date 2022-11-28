package promowarn.version3;

import promowarn.fp.core.Pair;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.BiFunction;
import java.util.Consumer;
import java.util.Function;
import promowarn.version1.DataProvider;
import promowarn.version1.Faculty;
import promowarn.version1.Promotion;
import promowarn.version1.PromotionWithDelegate;
import promowarn.version1.Student;
import promowarn.common.io.*;
import promowarn.common.mail.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class App {
    private static final Logger LOGGER = LogManager.getLogger(App.class.getName());

    private static String koMessage(final Promotion p, final double m) {
        return String.format("promotion %d -- risk (%.2f)", p.id(), m);
    }

    private static String okMessage(final Promotion p, final double m) {
        return String.format("promotion %d -- no risk (%.2f)", p.id(), m);
    }
    private static final Optional<Double> average(final Promotion p) {
         //v1 : pb, renvoit 0 et non Optional.empty si pas de moyenne
         final Double avg = p.students().stream() // Stream<Student>
                 .map(Student::grade) // Stream<Optional<Double>>
                 .flatMap(Optional<Double>::stream) // Stream<Double>
                 .collect(Collectors.averagingDouble(Double::valueOf));
        
        return avg.isEmpty() ? Optional.empty() : Optional.of(avg.getAsDouble());
    }


    // Généralisation
    private static final String alertMessage(double limit, BiFunction<Promotion, Double, String> okMessage,
            BiFunction<Promotion, Double, String> koMessage, Promotion p, double avg) {
        return avg < limit ? koMessage(p, avg) : okMessage(p, avg);
    }



    // Généralisation
    private static final Optional<String> alertTitle(final Promotion p) {
        return average(p).map(avg -> alertMessage(10, App::okMessage, App::koMessage, p, avg));
    }
    
    private static final Optional<EMailAddress> delegateEMail(final PromotionWithDelegate p) {
        return p.delegate().map(Student::email);
    }

    private static final Optional<Pair<EMailCategory, EMail>> createEMail(final PromotionWithDelegate p) {
        return delegateEMail(p).flatMap(email -> 
              alertTitle(p).map(title -> 
              new Pair<>(EMailCategory.DRAFT, new EMail(email, title))));
    }


    // Généralisation
    private final static Function<Consumer<Pair<EMailCategory, EMail>>, Consumer<Optional<Pair<EMailCategory, EMail>>>> lift = c -> o -> {
        if (o.isPresent())
            c.accept(o.get());
    };


    // Généralisation
    private static void alert(final MailBox box, final Faculty f) {
        f.promotions().stream() 
                .map(App::createEMail) 
                .forEach(lift.apply(p -> box.prepare(p.fst(), p.snd())));
    }

    public static void main(final String[] args) {
        final DataProvider dao = new DataProvider();
        final EMailService service = new LoggerFakeEMailService(LOGGER);
        final MailBox mailbox = new MailBox(service);
        alert(mailbox, dao.faculty(1));
        LOGGER.info(mailbox);
        mailbox.sendAll();
        LOGGER.info(mailbox);
    }
}
