package net.coding.program;

import android.test.AndroidTestCase;

import net.coding.program.common.CommentBackup;
import net.coding.program.common.HtmlContent;

/**
 * Created by chaochen on 15/1/27.
 */
public class CommonTest extends AndroidTestCase {

    public void testSave() {
        CommentBackup commentBackup = CommentBackup.getInstance();

        int id = 11;
        int global = 22;
        String comment = "very good";

        CommentBackup.BackupParam param = new CommentBackup.BackupParam(CommentBackup.Type.Maopao,
                id, global);

        commentBackup.save(param, comment);
        assertEquals(commentBackup.load(param), comment);

        commentBackup.delete(param);
        assertEquals(commentBackup.load(param), "");


        commentBackup.save(param, comment);

        String comment2 = "早上好";
        CommentBackup.BackupParam param2 = new CommentBackup.BackupParam(CommentBackup.Type.Maopao,
                id, global);
        commentBackup.save(param2, comment2);
        assertEquals(commentBackup.load(param), comment2);
        assertEquals(commentBackup.load(param2), comment2);

        commentBackup.delete(param2);
        assertEquals(commentBackup.load(param), "");
    }

    public void testMaopaoContent() {
        String content = "<p>最近的烦心事实在太多，完全不能静下心来写日报，好几次写了半天都全部删除。先沉淀一段时间再写吧。谢谢一些泡友的默默支持！</p> <p><a href=\"https://dn-coding-net-production-pp.qbox.me/b60e8f19-727a-41d9-a1ee-2d9729d425ef.png\" target=\"_blank\" class=\"bubble-markdown-image-link\" rel=\"nofollow\"><img src=\"https://dn-coding-net-production-pp.qbox.me/b60e8f19-727a-41d9-a1ee-2d9729d425ef.png\" alt=\"图片\" class=\" bubble-markdown-image\"></a> </p> <p><a href=\"https://dn-coding-net-production-pp.qbox.me/1c2d7a36-832c-439f-86b1-805dc7e78b36.png\" target=\"_blank\" class=\"bubble-markdown-image-link\" rel=\"nofollow\"><img src=\"https://dn-coding-net-production-pp.qbox.me/1c2d7a36-832c-439f-86b1-805dc7e78b36.png\" alt=\"图片\" class=\" bubble-markdown-image\"></a> </p> <p><a href=\"https://dn-coding-net-production-pp.qbox.me/238391ae-b564-4def-a3bd-f6c9bed65642.png\" target=\"_blank\" class=\"bubble-markdown-image-link\" rel=\"nofollow\"><img src=\"https://dn-coding-net-production-pp.qbox.me/238391ae-b564-4def-a3bd-f6c9bed65642.png\" alt=\"图片\" class=\" bubble-markdown-image\"></a></p>";
        assertEquals(HtmlContent.parseMaopao(content).text, "最近的烦心事实在太多，完全不能静下心来写日报，好几次写了半天都全部删除。先沉淀一段时间再写吧。谢谢一些泡友的默默支持！");

        String content1 = "<p>美不美</p> <p><a href=\"https://dn-coding-net-production-pp.qbox.me/455bd8a2-ef1d-4b64-8bd8-a472fe252404.png\" target=\"_blank\" class=\"bubble-markdown-image-link\" rel=\"nofollow\"><img src=\"https://dn-coding-net-production-pp.qbox.me/455bd8a2-ef1d-4b64-8bd8-a472fe252404.png\" alt=\"图片\" class=\" bubble-markdown-image\"></a></p> <p><em>图片截自<a href=\"http://mp.weixin.qq.com/s?__biz=MjM5NjAxMTc3Mg==&amp;mid=220691222&amp;idx=1&amp;sn=8db504113e1a8f1353c478c131cf57e9&amp;scene=0&amp;key=dffc561732c22651c76c80efe336e2073d815cd0f7fa0ec51e66eff32a416d2afb398404eaf8faccf01b3606aacd2b69&amp;ascene=0&amp;uin=MjQzNDc1ODU0MA%3D%3D&amp;devicetype=iMac+MacBookAir6\" target=\"_blank\" class=\" auto-link\" rel=\"nofollow\">造字工房</a></em></p>";
        assertEquals(HtmlContent.parseToShareText(content1), "美不美 [图片] 图片截自造字工房");
    }

}
