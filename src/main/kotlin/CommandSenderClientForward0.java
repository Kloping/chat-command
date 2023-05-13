import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.permission.PermitteeId;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Member;
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
public class CommandSenderClientForward0 implements CommandSender {

    private CommandSenderClient senderClient;
    private Member member;

    public CommandSenderClientForward0(CommandSenderClient senderClient) {
        this.senderClient = senderClient;
        member = senderClient.getEvent().getSender();
    }

    @Nullable
    @Override
    public Bot getBot() {
        return member.getBot();
    }

    @NotNull
    @Override
    public String getName() {
        return member.getNameCard();
    }

    @Nullable
    @Override
    public Contact getSubject() {
        return member.getGroup();
    }

    @Nullable
    @Override
    public User getUser() {
        return member;
    }

    @Nullable
    @Override
    public Object sendMessage(@NotNull String s, @NotNull Continuation<? super MessageReceipt<? extends Contact>> continuation) {
        ForwardMessageBuilder builder = new ForwardMessageBuilder(member.getGroup());
        builder.add(member.getBot(), new PlainText(s));
        ForwardMessage fm = builder.build();
        return member.getGroup().sendMessage(fm);
    }

    @Nullable
    @Override
    public Object sendMessage(@NotNull Message message, @NotNull Continuation<? super MessageReceipt<? extends Contact>> continuation) {
        ForwardMessageBuilder builder = new ForwardMessageBuilder(member.getGroup());
        builder.add(member.getBot(), message);
        ForwardMessage fm = builder.build();
        return member.getGroup().sendMessage(fm);
    }

    public PermitteeId getPermitteeId() {
        return ConsoleCommandSender.INSTANCE.getPermitteeId();
    }

    @NotNull
    @Override
    public CoroutineContext getCoroutineContext() {
        return member.getBot().getCoroutineContext();
    }
}
