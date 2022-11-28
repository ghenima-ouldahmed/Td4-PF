package promowarn.version3;

import java.util.Optional;

public class PromotionWithDelegateImpl extends PromotionImpl implements PromotionWithDelegate {
    private Student delegate = null;

    public PromotionWithDelegateImpl(final int id) {
        super(id);
    }

    @Override
    public Optional<Student> delegate() {
        return Optional.ofNullable(delegate);
    }

    @Override
    public void chooseDelegate(final Student e) {
        if (students().contains(e)) {
            delegate = e;
        }
    }
}
