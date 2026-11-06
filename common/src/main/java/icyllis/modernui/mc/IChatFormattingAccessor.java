package icyllis.modernui.mc;

/**
 * Duck interface for {@link net.minecraft.ChatFormatting}, implemented by the mixin.
 * <p>
 * NOTE: this interface must NOT live in a mixin package, because it is referenced
 * directly by regular classes (see {@link MuiModApi}), which the mixin package forbids.
 */
public interface IChatFormattingAccessor {

    char mui$getCode();
}
