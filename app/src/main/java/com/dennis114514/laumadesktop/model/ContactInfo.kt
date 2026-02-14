package com.dennis114514.laumadesktop.model

/**
 * 联系人信息数据模型
 * 对应ContractInformation.json文件中的联系人数据结构
 */
data class ContactInfo(
    val name: String = "",      // 姓名
    val qq: String = "",        // QQ号
    val phone: String = "",     // 电话号码
    val image: String = "",     // 头像文件名
    val audio: String = ""      // 音频文件名（暂不使用）
)