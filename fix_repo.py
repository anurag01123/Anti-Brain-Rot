import re

with open("app/src/main/java/com/example/data/AppRepository.kt", "r") as f:
    content = f.read()

old_logic = """    suspend fun markContactCalled(phoneNumber: String, timestamp: Long) {
        val allContacts = appDao.getAllContactsSync()
        // Phones formats can differ, so let's do a loose matching or clean the number
        val cleanTarget = phoneNumber.replace(Regex("[^0-9+]"), "")
        val contactToUpdate = allContacts.find { 
            val cleanDbNum = it.phoneNumber.replace(Regex("[^0-9+]"), "")
            cleanDbNum == cleanTarget || cleanDbNum.endsWith(cleanTarget) || cleanTarget.endsWith(cleanDbNum)
        }
        if (contactToUpdate != null) {
            appDao.updateContactCallTime(contactToUpdate.phoneNumber, timestamp)
        }
    }"""

new_logic = """    suspend fun markContactCalled(phoneNumber: String, timestamp: Long) {
        val allContacts = appDao.getAllContactsSync()
        val contactToUpdate = allContacts.find { 
            android.telephony.PhoneNumberUtils.compare(it.phoneNumber, phoneNumber) || 
            (it.phoneNumber.replace(Regex("[^0-9]"), "").takeLast(7) == phoneNumber.replace(Regex("[^0-9]"), "").takeLast(7) && phoneNumber.replace(Regex("[^0-9]"), "").length >= 7)
        }
        if (contactToUpdate != null) {
            appDao.updateContactCallTime(contactToUpdate.phoneNumber, timestamp)
        }
    }"""

content = content.replace(old_logic, new_logic)

with open("app/src/main/java/com/example/data/AppRepository.kt", "w") as f:
    f.write(content)
