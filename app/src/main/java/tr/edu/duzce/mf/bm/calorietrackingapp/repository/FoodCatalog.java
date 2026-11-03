package tr.edu.duzce.mf.bm.calorietrackingapp.repository;

import java.util.ArrayList;
import java.util.List;

import tr.edu.duzce.mf.bm.calorietrackingapp.database.entity.Food;

public final class FoodCatalog {

    public static final int PREDEFINED_FOOD_COUNT = 103;

    private FoodCatalog() {
    }

    // sugar=şeker uyarısı (diyabet), salt=tuz uyarısı (tansiyon), fat=doymuş yağ uyarısı (kolesterol)
    public static List<Food> getPredefinedFoods() {
        List<Food> foods = new ArrayList<>();

        // KAHVALTI
        foods.add(makeFood(1,  "Simit",                "Sesame Bagel",                265, "BREAKFAST", false, true,  false));
        foods.add(makeFood(2,  "Poğaça",               "Poğaça (Pastry)",             320, "BREAKFAST", false, true,  true));
        foods.add(makeFood(3,  "Açma",                 "Açma (Soft Roll)",            290, "BREAKFAST", false, true,  true));
        foods.add(makeFood(4,  "Beyaz Peynir",         "White Cheese (Feta)",         264, "BREAKFAST", false, true,  false));
        foods.add(makeFood(5,  "Kaşar Peyniri",        "Yellow Cheese",               380, "BREAKFAST", false, true,  true));
        foods.add(makeFood(6,  "Yumurta (haşlanmış)",  "Boiled Egg",                  155, "BREAKFAST", false, false, false));
        foods.add(makeFood(7,  "Omlet",                "Omelette",                    154, "BREAKFAST", false, false, false));
        foods.add(makeFood(8,  "Menemen",              "Menemen (Egg & Tomato)",      130, "BREAKFAST", false, false, false));
        foods.add(makeFood(9,  "Bal",                  "Honey",                       304, "BREAKFAST", true,  false, false));
        foods.add(makeFood(10, "Reçel",                "Jam",                         250, "BREAKFAST", true,  false, false));
        foods.add(makeFood(11, "Tereyağı",             "Butter",                      717, "BREAKFAST", false, false, true));
        foods.add(makeFood(12, "Ekmek (beyaz)",        "White Bread",                 265, "BREAKFAST", false, false, false));
        foods.add(makeFood(13, "Ekmek (tam buğday)",   "Whole Wheat Bread",           247, "BREAKFAST", false, false, false));
        foods.add(makeFood(14, "Çay (sütsüz)",         "Black Tea",                     2, "BREAKFAST", false, false, false));
        foods.add(makeFood(15, "Süt (tam yağlı)",      "Whole Milk",                   61, "BREAKFAST", true,  false, false));
        foods.add(makeFood(60, "Gözleme (peynirli)",   "Cheese Gözleme",              230, "BREAKFAST", false, true,  true));
        foods.add(makeFood(61, "Sucuk",                "Turkish Sausage (Sucuk)",     450, "BREAKFAST", false, true,  true));
        foods.add(makeFood(62, "Tahin",                "Tahini",                      595, "BREAKFAST", false, false, false));
        foods.add(makeFood(63, "Pekmez",               "Grape Molasses",              280, "BREAKFAST", true,  false, false));
        foods.add(makeFood(64, "Yulaf Ezmesi",         "Oatmeal",                      68, "BREAKFAST", false, false, false));
        foods.add(makeFood(65, "Granola",              "Granola",                     387, "BREAKFAST", true,  false, false));
        foods.add(makeFood(66, "Zeytin (siyah)",       "Black Olive",                 115, "BREAKFAST", false, false, false));
        foods.add(makeFood(67, "Domates (taze)",       "Fresh Tomato",                 18, "BREAKFAST", false, false, false));
        foods.add(makeFood(68, "Salatalık",            "Cucumber",                     15, "BREAKFAST", false, false, false));
        foods.add(makeFood(69, "Kakaolu Krema",        "Chocolate Spread",            539, "BREAKFAST", true,  false, true));

        // ÖĞLE
        foods.add(makeFood(16, "Lahmacun",             "Lahmacun (Turkish Pizza)",    270, "LUNCH", false, true,  false));
        foods.add(makeFood(17, "Pide (peynirli)",      "Cheese Pide",                 280, "LUNCH", false, true,  true));
        foods.add(makeFood(18, "Döner (tavuk)",        "Chicken Doner",               215, "LUNCH", false, true,  false));
        foods.add(makeFood(19, "Döner (et)",           "Meat Doner",                  250, "LUNCH", false, true,  true));
        foods.add(makeFood(20, "İskender Kebap",       "İskender Kebab",              220, "LUNCH", false, true,  true));
        foods.add(makeFood(21, "Köfte",                "Meatball",                    235, "LUNCH", false, false, true));
        foods.add(makeFood(22, "Pilav (beyaz pirinç)", "White Rice",                  130, "LUNCH", false, false, false));
        foods.add(makeFood(23, "Bulgur Pilavı",        "Bulgur Pilaf",                112, "LUNCH", false, false, false));
        foods.add(makeFood(24, "Mercimek Çorbası",     "Red Lentil Soup",              56, "LUNCH", false, false, false));
        foods.add(makeFood(25, "Ezogelin Çorbası",     "Ezogelin Soup",                58, "LUNCH", false, false, false));
        foods.add(makeFood(26, "Tavuk Göğsü (ızgara)", "Grilled Chicken Breast",      165, "LUNCH", false, false, false));
        foods.add(makeFood(27, "Şiş Kebap",            "Shish Kebab",                 195, "LUNCH", false, false, false));
        foods.add(makeFood(28, "Salata (karışık)",     "Mixed Salad",                  20, "LUNCH", false, false, false));
        foods.add(makeFood(29, "Ayran",                "Ayran (Yogurt Drink)",         36, "LUNCH", false, false, false));
        foods.add(makeFood(30, "Cacık",                "Cacık (Yogurt & Cucumber)",    45, "LUNCH", false, false, false));
        foods.add(makeFood(70, "Mantı",                "Turkish Dumplings (Mantı)",   280, "LUNCH", false, false, true));
        foods.add(makeFood(71, "Yaprak Sarma",         "Stuffed Grape Leaves",        175, "LUNCH", false, false, false));
        foods.add(makeFood(72, "Kısır",                "Kısır (Bulgur Salad)",         97, "LUNCH", false, false, false));
        foods.add(makeFood(73, "Çoban Salatası",       "Shepherd Salad",               35, "LUNCH", false, false, false));
        foods.add(makeFood(74, "Patlıcan Kebap",       "Eggplant Kebab",              180, "LUNCH", false, true,  false));
        foods.add(makeFood(75, "Tarhana Çorbası",      "Tarhana Soup",                 48, "LUNCH", false, false, false));
        foods.add(makeFood(76, "Tavuk Çorbası",        "Chicken Soup",                 45, "LUNCH", false, false, false));
        foods.add(makeFood(77, "Piyaz",                "Bean Salad (Piyaz)",          130, "LUNCH", false, false, false));
        foods.add(makeFood(78, "Humus",                "Hummus",                      166, "LUNCH", false, false, false));

        // AKŞAM
        foods.add(makeFood(31, "Kuru Fasulye",         "White Bean Stew",             137, "DINNER", false, false, false));
        foods.add(makeFood(32, "Nohut Yemeği",         "Chickpea Stew",               164, "DINNER", false, false, false));
        foods.add(makeFood(33, "Mercimek Yemeği",      "Lentil Dish",                 116, "DINNER", false, false, false));
        foods.add(makeFood(34, "Etli Sebze (karışık)", "Mixed Vegetable with Meat",    95, "DINNER", false, false, false));
        foods.add(makeFood(35, "Izgara Balık",         "Grilled Fish",                150, "DINNER", false, false, false));
        foods.add(makeFood(36, "Hamsi Tava",           "Fried Anchovy",               190, "DINNER", false, true,  true));
        foods.add(makeFood(37, "Makarna (domates soslu)","Pasta (Tomato Sauce)",      158, "DINNER", false, false, false));
        foods.add(makeFood(38, "Börek (ıspanaklı)",    "Spinach Börek",               245, "DINNER", false, true,  true));
        foods.add(makeFood(39, "Türk Usulü Yoğurt",    "Turkish Yogurt",               61, "DINNER", false, false, false));
        foods.add(makeFood(40, "Domates Çorbası",      "Tomato Soup",                  42, "DINNER", false, false, false));
        foods.add(makeFood(79, "Karnıyarık",           "Karnıyarık (Stuffed Eggplant)",160, "DINNER", false, false, true));
        foods.add(makeFood(80, "Musakka",              "Moussaka",                    180, "DINNER", false, true,  true));
        foods.add(makeFood(81, "Sebze Güveç",          "Vegetable Casserole",          75, "DINNER", false, false, false));
        foods.add(makeFood(82, "Levrek (ızgara)",      "Grilled Sea Bass",            124, "DINNER", false, false, false));
        foods.add(makeFood(83, "Fırın Tavuk",          "Baked Chicken",               215, "DINNER", false, false, false));
        foods.add(makeFood(84, "Piliç But (ızgara)",   "Grilled Chicken Thigh",       185, "DINNER", false, false, false));
        foods.add(makeFood(85, "İmam Bayıldı",         "İmam Bayıldı (Braised Eggplant)", 95, "DINNER", false, false, true));
        foods.add(makeFood(86, "Hünkar Beğendi",       "Hünkar Beğendi",              200, "DINNER", false, true,  true));
        foods.add(makeFood(87, "Makarna (kremalı)",    "Creamy Pasta",                210, "DINNER", false, false, true));
        foods.add(makeFood(88, "Börek (etli)",         "Meat Börek",                  290, "DINNER", false, true,  true));

        // ATIŞTIRMALIK
        foods.add(makeFood(41, "Hamburger",            "Hamburger",                   295, "SNACK", false, true,  true));
        foods.add(makeFood(42, "Kola (330ml)",         "Cola (330ml)",                139, "SNACK", true,  false, false));
        foods.add(makeFood(43, "Ayçiçeği Çekirdeği",   "Sunflower Seeds",             584, "SNACK", false, true,  false));
        foods.add(makeFood(44, "Cips (patates)",       "Potato Chips",                536, "SNACK", false, true,  true));
        foods.add(makeFood(45, "Çikolata (sütlü)",     "Milk Chocolate",              535, "SNACK", true,  false, true));
        foods.add(makeFood(46, "Elma",                 "Apple",                        52, "SNACK", true,  false, false));
        foods.add(makeFood(47, "Muz",                  "Banana",                       89, "SNACK", true,  false, false));
        foods.add(makeFood(48, "Portakal",             "Orange",                       47, "SNACK", true,  false, false));
        foods.add(makeFood(49, "Üzüm",                 "Grape",                        67, "SNACK", true,  false, false));
        foods.add(makeFood(50, "Baklava",              "Baklava",                     428, "SNACK", true,  false, true));
        foods.add(makeFood(51, "Lokum",                "Turkish Delight",             322, "SNACK", true,  false, false));
        foods.add(makeFood(52, "Sütlaç",               "Rice Pudding",                122, "SNACK", true,  false, false));
        foods.add(makeFood(53, "Helva (tahin)",        "Halva",                       516, "SNACK", true,  false, true));
        foods.add(makeFood(54, "Kek",                  "Cake",                        350, "SNACK", true,  false, true));
        foods.add(makeFood(55, "Sandviç (tavuklu)",    "Chicken Sandwich",            210, "SNACK", false, false, false));
        foods.add(makeFood(56, "Pizza (dilim)",        "Pizza Slice",                 266, "SNACK", false, true,  true));
        foods.add(makeFood(57, "Tost",                 "Toast",                       230, "SNACK", false, true,  true));
        foods.add(makeFood(58, "Fındık",               "Hazelnut",                    628, "SNACK", false, false, false));
        foods.add(makeFood(59, "Ceviz",                "Walnut",                      654, "SNACK", false, false, false));
        foods.add(makeFood(89, "Dondurma",             "Ice Cream",                   207, "SNACK", true,  false, true));
        foods.add(makeFood(90, "Çilek",                "Strawberry",                   32, "SNACK", true,  false, false));
        foods.add(makeFood(91, "Karpuz",               "Watermelon",                   30, "SNACK", true,  false, false));
        foods.add(makeFood(92, "Kavun",                "Melon",                        34, "SNACK", true,  false, false));
        foods.add(makeFood(93, "Şeftali",              "Peach",                        39, "SNACK", true,  false, false));
        foods.add(makeFood(94, "Armut",                "Pear",                         57, "SNACK", true,  false, false));
        foods.add(makeFood(95, "Mısır (patlamış)",     "Popcorn",                     375, "SNACK", false, false, false));
        foods.add(makeFood(96, "Yer Fıstığı",          "Peanut",                      567, "SNACK", false, false, false));
        foods.add(makeFood(97, "Badem",                "Almond",                      579, "SNACK", false, false, false));
        foods.add(makeFood(98, "Kaju",                 "Cashew",                      553, "SNACK", false, false, false));
        foods.add(makeFood(99, "Portakal Suyu",        "Orange Juice",                 45, "SNACK", true,  false, false));
        foods.add(makeFood(100,"Türk Kahvesi",         "Turkish Coffee",                2, "SNACK", false, false, false));
        foods.add(makeFood(101,"Limonata",             "Lemonade",                     40, "SNACK", true,  false, false));
        foods.add(makeFood(102,"Wafer",                "Wafer",                       489, "SNACK", true,  false, true));
        foods.add(makeFood(103,"Gofret (kremalı)",     "Cream Waffle",                472, "SNACK", true,  false, true));

        return foods;
    }

    private static Food makeFood(int id, String nameTr, String nameEn, int cal100g,
                                  String category, boolean sugarWarning, boolean saltWarning, boolean fatWarning) {
        Food food = new Food();
        food.setId(id);
        food.setNameTr(nameTr);
        food.setNameEn(nameEn);
        food.setCaloriesPer100g(cal100g);
        food.setCategory(category);
        food.setPredefined(true);
        food.setSugarWarning(sugarWarning);
        food.setSaltWarning(saltWarning);
        food.setFatWarning(fatWarning);
        return food;
    }
}
