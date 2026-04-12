package com.freemusic.data

import com.freemusic.util.ArtistNameNormalizer

/**
 * 全球常见歌手名单
 * 用于歌手名字标准化和自动识别
 */
object KnownArtists {
    
    /**
     * 华语歌手
     */
    val chineseArtists = listOf(
        // 华语流行
        "周杰伦", "林俊杰", "陈奕迅", "张学友", "刘德华", "郭富城", "黎明",
        "王菲", "王力宏", "陶喆", "潘玮柏", "林宥嘉", "方大同", "张惠妹",
        "蔡依林", "萧亚轩", "梁静茹", "孙燕姿", "莫文蔚", "邓紫棋", "G.E.M.",
        "张靓颖", "李宇春", "周笔畅", "华晨宇", "毛不易", "赵雷", "马良",
        "崔健", "汪峰", "那英", "杨坤", "张杰", "陈奕迅", "古巨基",
        "胡夏", "许嵩", "徐佳莹", "林志炫", "杨宗纬", "张碧晨", "胡润",
        "李荣浩", "薛之谦", "毛不易", "G.E.M.邓紫棋", "邓紫棋",
        "TFBOYS", "王俊凯", "王源", "易烊千玺",
        "蔡徐坤", "张艺兴", "黄子韬", "吴亦凡", "鹿晗",
        "IU", "李知恩", "权志龙", "GDG", "BIGBANG", "防弹少年团", "BTS",
        "EXO", "伯贤", "灿烈", "世勋", "SUHO", "KAI", "CHEN", "DO",
        "BLACKPINK", "JISOO", "JENNIE", "ROSÉ", "LISA",
        "TWICE", "子瑜", "多贤", "彩瑛", "娜琏", "定延", "MOMO", "SANA", "弥生", "子瑜",
        "Red Velvet", "IRENE", "SEULGI", "WENDY", "JOY", "YERI",
        "IU", "李知恩", "李宣美", "泫雅", "宣美", "请夏", "泰妍", "林允儿", "郑秀晶", "郑秀妍",
        "王心凌", "徐佳莹", "江语晨", "田馥甄", "HEBE", "林宥嘉", "萧敬腾",
        "A-LIN", "黄丽玲", "林俊杰", "JJ林俊杰", "JJ", "许嵩", "徐良", "后弦",
        "大张伟", "花儿乐队", "张震岳", "罗大佑", "李宗盛", "周华健", "齐秦",
        "五月天", "阿信", "怪兽", "石头", "冠佑", "玛莎",
        "苏打绿", "吴青峰", "林宥嘉", "张悬", "安溥",
        "痛仰", "新裤子", "刺猬", "海龟先生", "旅行团", "逃跑计划",
        "李健", "老狼", "朴树", "叶蓓", "水木年华", "清华才子",
        "凤凰传奇", "筷子兄弟", "小苹果", "慕容晓晓", "郑源", "冷漠",
        "汪苏泷", "许嵩", "徐良", "后弦", "本兮", "单色凌", "阿悄",
        "艾辰", "艾热", "阿杜", "阿桑", "刀郎", "云朵", "冷漠", "冷漠",
        "张学友", "谭咏麟", "张国荣", "梅艳芳", "Beyond", "黄家驹",
        "陈慧娴", "叶倩文", "林子祥", "草蜢", "郑智化", "罗大佑",
        "陶喆", "张惠妹", "伍佰", "齐秦", "熊天平", "许茹芸", "许美静",
        "李玟", "CoCo", "张惠妹", "A-LIN", "黄丽玲",
        "田馥甄", "HEBE", "林宥嘉", "萧敬腾", "方大同", "韦礼安",
        "范晓萱", "徐佳莹", "杨乃文", "戴佩妮", "周华健", "任贤齐",
        "范晓萱", "小S", "徐熙媛", "大小S", "蔡依林", "孙燕姿",
        "梁静茹", "刘若英", "奶茶", "林志炫", "陈洁仪", "A-LIN",
        
        // 乐队
        "五月天", "苏打绿", "SHE", "S.H.E", "HEBE", "ELLA", "SELINA",
        "飞儿乐团", "F.I.R", "信乐团", "动力火车", "周杰伦", "林俊杰",
        "草蜢", "Beyond", "披头士", "The Beatles", "皇后乐队", "Queen",
        
        // 更多华语
        "邓紫棋", "G.E.M", "GEM", "GEM邓紫棋",
        "李宇春", "春春", "周笔畅", "笔笔", "张靓颖", "何洁", "黄雅莉",
        "曾轶可", "许飞", "纪敏佳", "黄英", "叶一茜", "朱雅蕾",
        "刘德华", "华仔", "黎明", "郭富城", "张学友", "四大天王",
        "周润发", "郑少秋", "郑中基", "陈冠希", "余文乐", "陈奕迅",
        
        // 粤语歌手
        "陈奕迅", "EASON", "Eason", "陈奕迅", "Eason Chan",
        "张学友", "Jacky Cheung", "Jacky",
        "刘德华", "Andy Lau", "Andy",
        "郭富城", "Aaron Kwok",
        "黎明", "Leon Lai",
        "林俊杰", "JJ Lin",
        "容祖儿", "Joey Yung",
        "Twins", "阿Sa", "阿娇",
        "谢霆锋", "Nicholas Tse",
        "古巨基", "Leo Ku",
        "郑中基", "Ronald Cheng",
        "陈慧琳", "Kelly Chen",
        "郑秀文", "Sammi Cheng",
        "杨千嬅", "Miriam Yeung",
        "梁咏琪", "Gigi Leung",
        "许志安", "Andy Hui",
        "李克勤", "Hacken Lee",
        "陈小春", "Jordan Chan",
        "郑伊健", "Ekin Cheng",
        "林晓培", "Jamie",
        "周慧敏", "Vivian Chow",
        "王菲", "Faye Wong", "Faye",
        "莫文蔚", "Karen Mok",
        "草蜢", "Grass Hopper",
        
        // 台语/闽南语歌手
        "江蕙", "黄乙玲", "洪荣宏", "陈淑桦", "江淑娜",
        "施孝荣", "张韶涵", "范晓萱", "黄小琥", "秀兰不孕",
        
        // 新加坡/马来西亚华语
        "林俊杰", "JJ", "JJ林俊杰", "蔡健雅", "Tanya", "孙燕姿", "伍佰",
        "陶喆", "光良", "品冠", "梁静茹", "李佳薇", "徐佳莹",
        
        // 其他
        "王心凌", "张惠妹", "A-LIN", "黄丽玲", "林宥嘉", "萧敬腾"
    )
    
    /**
     * 欧美歌手
     */
    val westernArtists = listOf(
        // Pop
        "Taylor Swift", "Ed Sheeran", "Adele", "Billie Eilish", "Ariana Grande",
        "Lady Gaga", "Beyoncé", "Rihanna", "Justin Bieber", "Selena Gomez",
        "Katy Perry", "Kesha", "Pink", "Alicia Keys", "Christina Aguilera",
        "Miley Cyrus", "Britney Spears", "Madonna", "Whitney Houston", "Michael Jackson",
        "Bruno Mars", "Post Malone", "Shawn Mendes", "Camila Cabello", "Dua Lipa",
        "Doja Cat", "Olivia Rodrigo", "Megan Thee Stallion", "Cardi B", "Nicki Minaj",
        "The Weeknd", "Drake", "Travis Scott", "Kendrick Lamar", "Eminem", "Jay-Z",
        "Kanye West", "Kylie Jenner", "BTS", "Blackpink",
        
        // Rock
        "The Beatles", "Queen", "Led Zeppelin", "Pink Floyd", "The Rolling Stones",
        "U2", "Coldplay", "Radiohead", "Nirvana", "Foo Fighters",
        "Linkin Park", "Green Day", "My Chemical Romance", "Panic! At The Disco",
        "Fall Out Boy", "Paramore", "The Killers", "Arctic Monkeys",
        "Oasis", "Blur", "Gorillaz", "Red Hot Chili Peppers",
        "Metallica", "AC/DC", "Bon Jovi", "Guns N' Roses", "Deep Purple",
        "Scorpions", "Europe", "The Eagles", "Creedence Clearwater Revival",
        
        // R&B / Soul
        "Usher", "Chris Brown", "Ne-Yo", "John Legend", "Alicia Keys",
        "Beyoncé", "Rihanna", "SZA", "H.E.R.", "Jazmine Sullivan",
        "Mary J. Blige", "Boyz II Men", "Babyface", "D'Angelo",
        "Frank Ocean", "Sade", "Adele", "Sam Smith", "Lizzo",
        
        // Electronic / Dance
        "Calvin Harris", "David Guetta", "Avicii", "Zedd", "Marshmello",
        "Tiësto", "Hardwell", "Afrojack", "Martin Garrix", "Kygo",
        "Deadmau5", "Skrillex", "Diplo", "DJ Snake", "Major Lazer",
        "The Chainsmokers", "Rita Ora", "Ellie Goulding", "Kesha",
        
        // Hip-Hop / Rap
        "Eminem", "Jay-Z", "Kanye West", "Drake", "Kendrick Lamar",
        "Travis Scott", "Future", "Migos", "Cardi B", "Nicki Minaj",
        "Lil Wayne", "Snoop Dogg", "50 Cent", "Kanye West",
        "Post Malone", "Juice WRLD", "Lil Uzi Vert", "Lil Nas X",
        
        // Country
        "Taylor Swift", "Shania Twain", "Dolly Parton", "Garth Brooks",
        "Carrie Underwood", "Blake Shelton", "Luke Bryan", "Kenny Chesney",
        "Florida Georgia Line", "Sam Hunt", "Kacey Musgraves",
        
        // Jazz / Blues
        "Miles Davis", "John Coltrane", "Louis Armstrong", "Ella Fitzgerald",
        "Billie Holiday", "Nina Simone", "Ray Charles", "B.B. King",
        "Stevie Wonder", "Amy Winehouse",
        
        // Classical / Soundtrack
        "Andrea Bocelli", "Sarah Brightman", "Josh Groban", "Celine Dion",
        "Il Divo", "Il Volo", "Sarah McLachlan", "Enya", "Sissel",
        
        // K-Pop
        "BTS", "BLACKPINK", "TWICE", "EXO", "Red Velvet", "NCT",
        "IU", "Lee Seung Gi", "Park Hyo Shin", "Ahn Jae Wook",
        "Big Bang", "G-Dragon", "T.O.P", "Taeyang", "Daesung",
        "2NE1", "Wonder Girls", "Super Junior", "Girls' Generation",
        "SHINee", "f(x)", "TVXQ", "JYJ",
        
        // J-Pop
        "Ayumi Hamasaki", "Hikaru Utada", "X Japan", "L'Arc~en~Ciel",
        "B'z", "Mr.Children", "Southern All Stars", "AKB48",
        "Arashi", "KAT-TUN", "NEWS", "Hey! Say! JUMP",
        "ONE OK ROCK", "RADWIMPS", "YOASOBI", "Ado",
        
        // Latin
        "Shakira", "Jennifer Lopez", "Ricky Martin", "Enrique Iglesias",
        "Daddy Yankee", "J Balvin", "Ozuna", "Bad Bunny",
        "Maluma", "Nicky Jam", "Seanium", "Karol G",
        "Luis Fonsi", "Despacito", "Bomba Estereo", "J Balvin",
        
        // 更多
        "David Bowie", "Freddie Mercury", "Elvis Presley", "Prince",
        "Stevie Nicks", "Bruce Springsteen", "Tom Petty", "Bob Dylan",
        "Joni Mitchell", "Patti Smith", "Carole King", "James Taylor",
        "Phil Collins", "Genesis", "Peter Gabriel", "Sting", "The Police",
        "Duran Duran", "Depeche Mode", "New Order", "Erasure",
        "The Cure", "Siouxsie and the Banshees", "Joy Division",
        "Wham!", "George Michael", "Robbie Williams", "Take That",
        "Oasis", "Liam Gallagher", "Noel Gallagher", "Oasis",
        "One Direction", "Harry Styles", "Zayn Malik", "Niall Horan",
        "Celine Dion", "Sarah McLachlan", "Nora Jones", "Norah Jones",
        "Amy Winehouse", "Adele", "Sam Smith", "Ellie Goulding",
        "Rita Ora", "Charli XCX", "Charli Puth", "Charlie Puth",
        "Lorde", "Sia", "Maggie Rogers", "Clairo", "Lana Del Rey",
        "Moby", "Daft Punk", "Justice", "Phoenix", "Air",
        "Massive Attack", "Portishead", "Tricky", "Björk",
        "The Cranberries", "Sinéad O'Connor", "Alanis Morissette",
        "Sheryl Crow", "Natalie Merchant", "Melissa Etheridge",
        "A-ha", "Roxette", "R.E.M.", "The Cranberries",
        "Van Morrison", "Rod Stewart", "Elton John", "George Michael",
        "Dolly Parton", "Willie Nelson", "Johnny Cash", "Patsy Cline"
    )
    
    /**
     * 日韩歌手
     */
    val koreanJapaneseArtists = listOf(
        // K-Pop
        "BTS", "防弹少年团", "RM", "JIN", "SUGA", "J-HOPE", "JIMIN", "V", "JUNG KOOK",
        "BLACKPINK", "JISOO", "JENNIE", "ROSÉ", "LISA",
        "TWICE", "娜琏", "定延", "MOMO", "SANA", "JIHYO", "MINA", "DAHYUN", "CHAEHYUNG", "TZUYU",
        "EXO", "SUHO", "CHANYEOL", "BAEKHYUN", "XIUMIN", "LAY", "CHEN", "DO", "KAI", "SEHUN",
        "Red Velvet", "IRENE", "SEULGI", "WENDY", "JOY", "YERI",
        "NCT 127", "NCT DREAM", "WAY V", "NCT",
        "IU", "李知恩", "Lee Ji-eun",
        "BIGBANG", "G-DRAGON", "T.O.P", "TAEYANG", "DAESUNG", "SEUNGRI",
        "2NE1", "CL", "MINZY", "PARK BOM", "SANDARA PARK",
        "Super Junior", "SJ", "利特", "希澈", "艺声", "金钟云", "申东熙", "李赫宰", "李东海", "崔始源", "金厉旭", "曺圭贤",
        "少女时代", "Girls' Generation", "泰妍", "允儿", "孝渊", "侑莉", "徐贤", "Tiffany", "Sunny", "Yuri",
        "SHINee", "温流", "钟铉", "KEY", "MINHO", "泰民",
        "f(x)", "LUNA", "AMBER", "雪莉", "郑秀晶",
        "TVXQ", "JYJ", "东方神起",
        "GOT7", "iKON", "WINNER", "MONSTA X", "STRAY KIDS",
        "SEVENTEEN", "ATEEZ", "LOONA", "GFRIEND", "WJSN",
        "宣美", "李宣美", "泫雅", "金泫雅", "请夏", "郑恩地", "AOA",
        
        // J-Pop
        "Ado", "YOASOBI", "Official髭男dism", "LiSA", "Aimer",
        "RADWIMPS", "ONE OK ROCK", "BUMP OF CHICKEN", "MY FIRST STORY",
        "L'Arc~en~Ciel", "HYDE", "Ken", "Tetsuya", "Yukihiro",
        "X Japan", "Yoshiki", "Hide", "Pata", "Taiji", "Saga",
        "B'z", "松本孝弘", "稻叶浩志",
        "Mr.Children", "小林武史", "宫本崇弘",
        "Southern All Stars", "桑田佳祐",
        "AKB48", "SKE48", "NMB48", "HKT48", "Nogizaka46", "Keyakizaka46",
        "Arashi", "岚", "松本润", "樱井翔", "相叶雅纪", "二宫和也", "大野智",
        "KAT-TUN", "NEWS", "Hey! Say! JUMP", "Kanjani Eight",
        "山下智久", "龟梨和也", "赤西仁", "锦户亮", "田口智则",
        "宇多田光", "Hikaru Utada", "椎名林檎", "奥华子", "gal会",
        "中岛美雪", "松任谷由实", "安全地带", "药师丸博子",
        "Hi-standard", "L'Arc~en~Ciel", "THE MAD CAPSULE MARKETS",
        "GReeeeN", "可苦可乐", "生物股长", "_YUKI", "JUDY AND MARY"
    )
    
    /**
     * 所有歌手名单（合并）
     */
    val allArtists: List<String> by lazy {
        (chineseArtists + westernArtists + koreanJapaneseArtists)
            .map { ArtistNameNormalizer.normalize(it) }
            .distinct()
            .sorted()
    }
    
    /**
     * 快速查找某歌手是否存在
     */
    fun contains(name: String): Boolean {
        val normalized = ArtistNameNormalizer.normalize(name)
        return allArtists.any { artist ->
            val normalizedArtist = ArtistNameNormalizer.normalize(artist)
            normalizedArtist == normalized ||
            normalizedArtist.contains(normalized, ignoreCase = true) ||
            normalized.contains(normalizedArtist, ignoreCase = true)
        }
    }
    
    /**
     * 查找匹配的歌手
     */
    fun findMatch(name: String): String? {
        if (name.isBlank()) return null
        val normalized = ArtistNameNormalizer.normalize(name)
        return ArtistNameNormalizer.findBestMatch(normalized, allArtists)
    }
}
