This file is encoded in UTF-8

联系人配置指南
请使用MT管理器打开与此文件在相同目录下的ContractInformation.json，然后你将会看到以下内容（可以用双指缩放调整为合适的大小）

  {
    "name": "",     ---->这是显示的姓名
    "QQ": "",       ---->这是QQ号，用于视频通话，如无需求可不填写
    "Phone": "",    ---->这是手机号，在拨打电话时会使用此号码
    "Image": "",    ---->这是显示的头像，这里只填写文件名，图片文件放在同目录下的Images文件夹内，名称要与这里填写的一致
    "Audio": ""     ---->这是语音播报功能的音频文件名，（尚未实装，故留空）
  }

（所有信息要填在引号""内）

Tip：可以使用MT管理器的批量重命名功能对大量图片进行管理
请将图片裁剪成1:1的大小（正方形）以显示完整图片

每个花括号{}括起来的项目为1组联系人信息
在内置示例文件中有四组空信息，可以根据实际需求自行增删（复制粘贴），每组数据之间用英文逗号,分隔（即在花括号}后面加一个逗号,，但最后一组数据不要加。）
所有数据需要在方括号[]内（示例文件中已有）

每组联系人信息就是一个对象，每个对象内部有几个值，用引号""包裹的是字符串值，本应用只需要字符串值。
冒号:前面的值类似于”变量“，可以被软件读取，冒号后面的是”赋值“，就是这个”变量“的具体内容。为了使代码美观，冒号后面一般空一格。
每组数据必须在花括号{}内，所有数据需要在方括号[]内，称为一个数组。
（中文与英文内容相同）

注意：请务必在联系人信息配置无误，软件可以正常使用之后，再把本软件设置为默认启动器！
如果不慎导致手机无法使用，请下拉通知栏，通过系统提供的快捷按钮转到“设置”应用，然后卸载本软件！




(English version is as same as Chinese  version)
Contact Configuration Guide
Please use MT Manager to open ContactInformation.json (located in the same directory as this file).
You will see content similar to the following (you can use pinch-to-zoom gestures to adjust the display size)

{
    "name": "",     ----> Display name
    "QQ": "",       ----> QQ number for video calls (optional; leave blank if not needed)
    "Phone": "",    ----> Phone number used for making calls
    "Image": "",    ----> Profile image filename (place the image file in the "Images" folder within the same directory; the filename here must match exactly)
    "Audio": ""     ----> Audio filename for voice announcement feature (not yet implemented; leave blank)
}

(All information must be entered within the quotation marks "")

Tip: You can use MT Manager's batch renaming feature to efficiently manage a large number of images.
Please cut the image to the size of 1:1(Square) to make the display complete

Each set of contact information is enclosed in curly braces {}.
The built-in example file contains four empty contact entries. You may add or remove entries as needed (via copy-paste). Separate each entry with an English comma , (i.e., add a comma after the closing brace }, except after the last entry).
All entries must be enclosed within square brackets [] (already provided in the example file).

Each contact entry is a JSON object. Within each object:
Values wrapped in quotation marks "" are strings—this application only requires string values.
The text before each colon : acts as a "key" that the app reads; the text after the colon is the "value" assigned to that key.
For readability, it is customary to include a single space after each colon.
All individual contact objects must be placed within curly braces {}, and the entire collection must be wrapped in square brackets [], forming a JSON array.

ATTENTION:Please set this app as a default launcher AFTER THE CONTACT INFORMATION IS NOT WRONG and the app can work properly.
If your phone becomes unusable by mistake, please pull down the notification bar, use the shortcut button provided by the system to go to the 'Settings' app, and then uninstall this software!