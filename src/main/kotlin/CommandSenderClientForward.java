import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.permission.AbstractPermitteeId;
import net.mamoe.mirai.console.permission.PermitteeId;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author github.kloping
 */
public class CommandSenderClientForward implements CommandSender {

    private MemberCommandSender sender;

    public CommandSenderClientForward(MemberCommandSender sender) {
        this.sender = sender;
    }

    @Nullable
    @Override
    public Bot getBot() {
        return sender.getBot();
    }

    @NotNull
    @Override
    public String getName() {
        return sender.getName();
    }

    @Nullable
    @Override
    public Contact getSubject() {
        return sender.getGroup();
    }

    @Nullable
    @Override
    public User getUser() {
        return sender.getUser();
    }

    @Nullable
    @Override
    public Object sendMessage(@NotNull String s, @NotNull Continuation<? super MessageReceipt<? extends Contact>> continuation) {
        ForwardMessageBuilder builder = new ForwardMessageBuilder(sender.getGroup());
        builder.add(sender.getBot(), new PlainText(s));
        ForwardMessage fm = builder.build();
        return sender.getGroup().sendMessage(fm);
    }

    @Nullable
    @Override
    public Object sendMessage(@NotNull Message message, @NotNull Continuation<? super MessageReceipt<? extends Contact>> continuation) {
        ForwardMessageBuilder builder = new ForwardMessageBuilder(sender.getGroup());
        builder.add(sender.getBot(), message);
        ForwardMessage fm = builder.build();
        return sender.getGroup().sendMessage(fm);
    }

    public PermitteeId getPermitteeId() {
        return new AbstractPermitteeId.ExactMember(sender.getGroup().getId(), sender.getUser().getId());
    }

    @NotNull
    @Override
    public CoroutineContext getCoroutineContext() {
        return sender.getBot().getCoroutineContext();
    }
}
