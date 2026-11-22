package com.bolaenglish.app.data

import java.util.Locale

data class Bridge(val english: String, val hindi: String, val marathi: String) {
    fun forLanguage(language: NativeLanguage) = when (language) {
        NativeLanguage.HINDI -> hindi
        NativeLanguage.MARATHI -> marathi
        else -> english
    }
}

data class Module(
    val id: String,
    val title: String,
    val pattern: String,
    val rule: Bridge,
    val example: String,
    val meaning: Bridge,
    val question: Bridge,
    val answers: List<String>,
    val tip: Bridge,
    val speaking: Bridge
) {
    val target get() = answers.first()
    fun accepts(answer: String) = answers.any { normalize(it) == normalize(answer) }
}

private fun normalize(text: String) = text.lowercase(Locale.ROOT)
    .replace(Regex("[.,!?]"), "").replace('’', '\'').trim().replace(Regex("\\s+"), " ")

val curriculum = listOf(
    Module("routine", "Daily routines", "Who + base verb + when",
        Bridge("Use the simple present for habits. Add -s with he or she.", "रोज़ की आदतों के लिए सामान्य वर्तमान काल इस्तेमाल करें। I के साथ मूल क्रिया रखें और he या she के साथ उसमें -s जोड़ें।", "रोजची सवय सांगताना साधा वर्तमानकाळ वापरा. I सोबत मूळ क्रियापद येते. He किंवा she सोबत त्याला -s लावा."),
        "I walk to work every day", Bridge("This is part of my daily routine.", "मैं रोज़ पैदल काम पर जाता / जाती हूँ।", "मी रोज चालत कामावर जातो / जाते."),
        Bridge("Say that she takes the bus every morning.", "वह हर सुबह बस लेती है। इसे अंग्रेज़ी में कहें।", "ती दर सकाळी बसने जाते. हे इंग्रजीत सांगा."),
        listOf("She takes the bus every morning", "Every morning she takes the bus", "She takes a bus every morning"),
        Bridge("With she, take becomes takes.", "She के साथ take की जगह takes कहें।", "She नंतर take ऐवजी takes म्हणा."),
        Bridge("Describe two morning habits. Check the verb after I and she.", "अपनी और किसी सहेली की सुबह की एक-एक आदत बताएं। I और she के बाद क्रिया जाँचें।", "तुमची आणि एखाद्या मैत्रिणीची सकाळची एकेक सवय सांगा. I आणि she नंतरचे क्रियापद तपासा.")),
    Module("directions", "Polite requests and directions", "Could you + base verb + details?",
        Bridge("Use could you to make a polite request. In an indirect question, keep the subject before is.", "विनम्र अनुरोध के लिए Could you कहें। सवाल के अंदर where the station is वाला क्रम रखें।", "नम्र विनंतीसाठी Could you वापरा. प्रश्नाच्या आत where the station is असा शब्दक्रम ठेवा."),
        "Could you show me the way", Bridge("Ask someone to guide you.", "क्या आप मुझे रास्ता दिखा सकते हैं?", "तुम्ही मला रस्ता दाखवू शकाल का?"),
        Bridge("Ask politely where the station is. Begin with Could you tell me.", "Could you tell me से शुरू करके पूछें कि स्टेशन कहाँ है।", "Could you tell me ने सुरुवात करून स्टेशन कुठे आहे ते विचारा."),
        listOf("Could you tell me where the station is", "Could you please tell me where the station is"),
        Bridge("After tell me, say where the station is.", "Tell me के बाद where is the station नहीं, where the station is कहें।", "Tell me नंतर where the station is असा क्रम ठेवा."),
        Bridge("Ask a stranger for directions, then thank them. Listen for could you.", "किसी से रास्ता पूछें और धन्यवाद दें। अपने सवाल में could you सुनें।", "एखाद्या व्यक्तीला रस्ता विचारा आणि आभार माना. प्रश्नात could you वापरले का ते तपासा.")),
    Module("past", "Past experiences", "Who + past verb + finished time",
        Bridge("Use a past verb for a finished event. Go changes to went.", "बीती घटना के लिए क्रिया का भूतकाल लें। Go का भूतकाल went है।", "घडून गेलेली घटना सांगताना भूतकाळ वापरा. Go चे भूतकाळी रूप went आहे."),
        "We visited Pune last week", Bridge("The visit is over.", "हम पिछले हफ़्ते पुणे गए थे।", "आम्ही गेल्या आठवड्यात पुण्याला गेलो होतो."),
        Bridge("Tell a friend you went to the market yesterday.", "बताएं कि आप कल बाज़ार गए थे।", "तुम्ही काल बाजारात गेलात हे मित्राला सांगा."),
        listOf("I went to the market yesterday", "Yesterday I went to the market"),
        Bridge("Yesterday needs went here, even though the base verb is go.", "कल की पूरी हुई घटना में I went कहें।", "काल घडलेली घटना सांगताना I went म्हणा."),
        Bridge("Tell a friend where you went and what you saw. Check both past verbs.", "बताएं कि आप कहाँ गए और आपने क्या देखा। दोनों क्रियाओं का भूतकाल जाँचें।", "तुम्ही कुठे गेलात आणि काय पाहिलेत ते सांगा. दोन्ही क्रियापदे भूतकाळात आहेत का ते तपासा.")),
    Module("plans", "Future plans", "Who + am/is/are + going to + base verb",
        Bridge("Use going to for a plan you have already made.", "पहले से तय योजना के लिए am, is या are के बाद going to और मूल क्रिया रखें।", "आधीच ठरलेला बेत सांगायला am, is किंवा are नंतर going to आणि मूळ क्रियापद वापरा."),
        "We are going to travel tomorrow", Bridge("We have a plan for tomorrow.", "हम कल सफ़र करने वाले हैं।", "आम्ही उद्या प्रवास करणार आहोत."),
        Bridge("Use going to to say you plan to study tonight.", "Going to लगाकर कहें कि आप आज रात पढ़ने वाले हैं।", "Going to वापरून तुम्ही आज रात्री अभ्यास करणार आहात असे सांगा."),
        listOf("I am going to study tonight", "I'm going to study tonight", "Tonight I am going to study"),
        Bridge("Keep am before going and use study after to.", "I के बाद am रखें। To के बाद study कहें।", "I नंतर am ठेवा. To नंतर study वापरा."),
        Bridge("Describe two weekend plans. Include a time and a base verb.", "सप्ताहांत की दो योजनाएं बताएं। समय और मूल क्रिया शामिल करें।", "सुट्टीचे दोन बेत सांगा. वेळ आणि मूळ क्रियापद वापरा.")),
    Module("interview", "Workplace and interview confidence", "I can + skill. For example, I + evidence.",
        Bridge("Name a skill with can, then support it with an example.", "Can के साथ अपना कौशल बताएं, फिर पिछले काम से एक उदाहरण दें।", "Can वापरून तुमचे कौशल्य सांगा. त्यानंतर आधीच्या कामाचे एक उदाहरण द्या."),
        "I can explain complex ideas clearly", Bridge("This describes a communication skill.", "मैं कठिन बातें साफ़ ढंग से समझा सकता / सकती हूँ।", "मी अवघड कल्पना स्पष्टपणे समजावून सांगू शकतो / शकते."),
        Bridge("Use can to say you work well in a team.", "Can से बताएं कि आप टीम में अच्छी तरह काम कर सकते हैं।", "Can वापरून तुम्ही संघात चांगले काम करू शकता असे सांगा."),
        listOf("I can work well in a team", "I can work well on a team"),
        Bridge("Use work directly after can. Leave out to.", "Can के तुरंत बाद work कहें। बीच में to न लगाएं।", "Can नंतर थेट work वापरा. मध्ये to घालू नका."),
        Bridge("Give a two-sentence interview answer. Name a skill and a time you used it.", "इंटरव्यू में दो वाक्य बोलें। एक कौशल और उसके इस्तेमाल का उदाहरण दें।", "मुलाखतीत दोन वाक्ये बोला. एक कौशल्य आणि ते वापरल्याचे उदाहरण द्या."))
)
