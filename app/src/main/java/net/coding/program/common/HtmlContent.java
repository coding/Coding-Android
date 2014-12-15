package net.coding.program.common;

import net.coding.program.Global;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chaochen on 14-10-25.
 */
public class HtmlContent {
    public static Global.MessageParse parseMaopao(String s) {
        final String regx = "(?:<br>)? ?<a href=\".*?\" target=\"_blank\" class=\"bubble-markdown-image-link\".*?><img src=\"(.*?)\" alt=\"(.*?)\".*?></a>(?:<br>)? ?";
        return createMessageParse(s, regx);
    }

    public static Global.MessageParse parseMessage(String s) {
        final String regx = "<div class='message-image-box'><a href='.*?' target='_blank'><img class='message-image' src='(.*?)'/></a></div>";
        Global.MessageParse parse = createMessageParse(s, regx);
        if (parse.uris.size() > 0) {
            return parse;
        } else {
            return parseMaopao(s);
        }
    }

    public static String TYPE_IMAGE_HEAD = "imagetype:";

    public static String parseDynamic(String s) {
        return parseTaskComment(s).text;
    }

    public static Global.MessageParse parseTaskComment(String s) {
        Global.MessageParse parse = new Global.MessageParse();

        String regx = "(?:<br />)? ?<a href=\".*\" target=\"_blank\" class=\"bubble-markdown-image-link\".*?><img src=\"(.*)\" alt=\"(.*)\".*></a>(?:<br />)? ?";
        String replace = "[图片]";
        String replaceImage = s.replaceAll(regx, replace);

        regx = "<img class=\"emotion emoji\" src=\".*?\" title=\"(.*?)\">";
        replace = "<img src=\"$1\">";
        replaceImage = replaceImage.replaceAll(regx, replace);

        parse.text = replaceAllSpace(replaceImage);

        return parse;
    }

    private static Global.MessageParse createMessageParse(String s, String regx) {
        Global.MessageParse parse = new Global.MessageParse();

        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            parse.uris.add(matcher.group(1));
        }

        String replaceImage = s.replaceAll(regx, "");
        parse.text = replaceAllSpace(replaceImage);

        return parse;
    }

    private static String replaceAllSpace(String s) {
        return s.replaceAll("<p>|</p>", "").replaceAll("[ \\n]*$", "");
    }

    public static String createUserHtml(String globalKey, String name) {
        final String format = "<font color='#3bbd79'><a href=\"coding-net://UserDetailActivity_?name=%s\">%s</a></font>";
        return String.format(format, globalKey, name);
    }
}
