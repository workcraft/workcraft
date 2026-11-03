package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DialogUtilsSilentTests {

    @Test
    void testSilentModeIsScopedAndNested() {
        Assertions.assertFalse(DialogUtils.isSilent());
        DialogUtils.runSilently(() -> {
            Assertions.assertTrue(DialogUtils.isSilent());
            DialogUtils.runSilently(() -> Assertions.assertTrue(DialogUtils.isSilent()));
            Assertions.assertTrue(DialogUtils.isSilent());
        });
        Assertions.assertFalse(DialogUtils.isSilent());
    }

    @Test
    void testSilentModeIsResetAfterFailure() {
        Assertions.assertThrows(IllegalStateException.class, () -> DialogUtils.runSilently(() -> {
            throw new IllegalStateException();
        }));
        Assertions.assertFalse(DialogUtils.isSilent());
    }

}
