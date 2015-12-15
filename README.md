### Coding Android客户端代码说明

##编译环境
Android Studio 1.5.1，用gradle引用的许多第三方库，第一次加载会有点慢，加载完毕后要build一下，这些待下划线的类（比如 MainActivity_）会在build之后自动生成（因为用了gradle，所以**不支持eclipse**）。

##包说明
>common 基类和工具类  
>>comment 评论区  
>>enter 输入框  
>>network 对网络做了一点封装  
>>photopick 图片多选控件  
>>umeng 封装了umeng  

>hide 进入staging界面  
>maopao 冒泡界面  
>message 消息界面  
>model 一些数据结构  
>project 我的项目界面  
>setting 设置界面  
>task 我的任务界面  
>third 一些第三方代码  
>user 好友界面  

##一些觉得有必要提一下的
因为不想写一堆绑定函数，所以项目用了 [androidannotations](https://github.com/excilys/androidannotations)，如果以前没用过最好先看看。
显示图片用的[universal-image-loader](https://github.com/nostra13/Android-Universal-Image-Loader)，网络库用的[android-async-http](https://github.com/loopj/android-async-http)，因为登录以后保存的cookie都在[android-async-http](https://github.com/loopj/android-async-http)，有些图片需要登录后的cookie才能取到（例如项目文档里面的图片），这种情况就会用先用[android-async-http](https://github.com/loopj/android-async-http)下载图片（AttachmentimagePagerFragment.java）。

- Application用的是MyApp.java，用静态变量保存了用户信息，屏幕长宽等信息（理论上来说不是好的做法，但是这些基本都是只读的数据，而且并不大，所以就这么做了）。

- 跳转：Coding的url是可以通过正则匹配来确定需要跳转的页面和参数。比如

```
        // 项目讨论
        // https://coding.net/u/8206503/p/AndroidCoding/topic/9638?page=1
        final String topic = "^https://[\\w.-]*/u/([\\w.-]+)/p/([\\w.-]+)/topic/(\\w+)(?:\\?[\\w=&-]*)?$";
        pattern = Pattern.compile(topic);
        matcher = pattern.matcher(uriString);
        if (matcher.find()) {
            intent.setClass(context, TopicListDetailActivity_.class);
            TopicListDetailActivity.TopicDetailParam param =
                    new TopicListDetailActivity.TopicDetailParam(matcher.group(1),
                            matcher.group(2), matcher.group(3));
            intent.putExtra("mJumpParam", param);
            context.startActivity(intent);
            return;
        }
```
8206503是创建者的个性后缀，AndroidCoding是项目名称，topic表示这是一个讨论，9638是讨论的id号。


- 图文混排显示：服务端给我的是html，图片的链接通过正则表达式提取出来用单独的ImageView显示，文字和表情则保留下来，通过 Html.fromHtml() 转换成 Spannable，用TextView显示，表情的图片已经打包到了apk，TextView会直接在资源里面查找出来。

- 本地缓存：由 AccountInfo 实现，其实就是将数据类以文件的方式保存起来。

- 表情输入：在后台，表情其实是我们自定义的文字，没有用emoji，比方说笑脸，emoji是0x0001F604，我们的后台对应的是 :smile: ，用户用系统键盘输入emoji的时候，我会做一下替换，至于显示，因为android的TextView和Edit本来就支持显示Spannable，基本上就是将文字转成Spannable就可以做到图文混排了。自定义的表情键盘就是用pager做出来的，有一点麻烦的是要保证表情键盘和系统键盘不能同时出现，我的做法是通过监听屏幕高度变化来设置键盘的显示和隐藏，在魅族的手机上遇到了问题，估计是因为smartbar的原因，回调函数有时候没有被调用，我的解决方法是用handle发了一个延时消息，保证回调函数能被调用。

- 用户统计用的是[umeng](http://www.umeng.com/)，因为以前用过，觉得还好，所以就继续用了。

- 推送用的是腾讯的信鸽，之前用的是umeng推送，但有延迟有时候很大，而且用新帐号登录后还能收到旧帐号的推送，多种原因之下就换了。

##用到的一些开源库说明
见我写的[blog](http://blog.coding.net/blog/android-open-source-library)

##License
Coding is available under the MIT license. See the LICENSE file for more info.