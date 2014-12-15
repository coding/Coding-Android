package net.coding.program;

import net.coding.program.common.HtmlContent;

/**
 * Created by chaochen on 14-10-25.
 */
public class TestParse {

    public void testGlobal() {
        String maopaoImage = "<p>多张图<br /> <a href=\"https://dn-coding-net-production-pp.qbox.me/c944f1b2-a1f8-4f03-9a34-55f014cb36b2.png\" target=\"_blank\" class=\"bubble-markdown-image-link\" rel=\"nofollow\"><img src=\"https://dn-coding-net-production-pp.qbox.me/c944f1b2-a1f8-4f03-9a34-55f014cb36b2.png\" alt=\"图片\" class=\"bubble-markdown-image\" /></a> </p> <p><a href=\"https://dn-coding-net-production-pp.qbox.me/d2e7dcb4-70aa-4052-8cb5-7ab5938fe74d.png\" target=\"_blank\" class=\"bubble-markdown-image-link\" rel=\"nofollow\"><img src=\"https://dn-coding-net-production-pp.qbox.me/d2e7dcb4-70aa-4052-8cb5-7ab5938fe74d.png\" alt=\"图片\" class=\"bubble-markdown-image\" /></a> </p>";
        Global.MessageParse parse = HtmlContent.parseMaopao(maopaoImage);

    }
}
