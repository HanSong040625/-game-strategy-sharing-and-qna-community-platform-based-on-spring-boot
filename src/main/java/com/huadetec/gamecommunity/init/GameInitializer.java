// package com.huadetec.gamecommunity.init;

// import com.huadetec.gamecommunity.entity.Game;
// import com.huadetec.gamecommunity.repository.GameRepository;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// import java.util.Arrays;
// import java.util.List;

// /**
//  * 游戏数据初始化器
//  * 在应用启动时初始化一些游戏数据
//  */
// @Component  // 标记为Spring组件
// public class GameInitializer implements CommandLineRunner {

//     private static final Logger logger = LoggerFactory.getLogger(GameInitializer.class);

//     @Autowired
//     private GameRepository gameRepository;

//     @Override
//     public void run(String... args) throws Exception {
//         logger.info("开始初始化游戏数据");
//         initGameData();
//         logger.info("游戏数据初始化完成");
//     }

//     /**
//      * 初始化游戏数据
//      */
//     private void initGameData() {
//         // 检查是否已有游戏数据
//         if (gameRepository.count() > 0) {
//             logger.info("游戏表中已有数据，跳过初始化");
//             return;
//         }

//         // 创建初始游戏数据
//         Game game1 = new Game();
//         game1.setName("三角洲行动");
//         game1.setDescription("一款以现代战争为背景的第一人称射击游戏，具有高度的战术性和多人在线游戏体验。");
//         game1.setLogoUrl("/dist/images/sanjiaozhou/san_jiao_zhou_logo.jpg");
//         game1.setPosterUrl("/dist/images/sanjiaozhou/san_jiao_zhou_logo.jpg"); // 使用logo作为临时海报
//         game1.setCategories("射击,战术,多人");
//         game1.setIsFeatured(true);

//         Game game2 = new Game();
//         game2.setName("部落冲突");
//         game2.setDescription("一款经典的策略战争游戏，玩家可以建设村庄并与其他玩家战斗。");
//         game2.setLogoUrl("/dist/images/buluochongtu/buluochongtu-image.svg");
//         game2.setPosterUrl("/dist/images/buluochongtu/buluochongtu-image.svg");
//         game2.setCategories("策略,战争");
//         game2.setIsFeatured(false);

//         Game game3 = new Game();
//         game3.setName("海盗奇兵");
//         game3.setDescription("一款海洋主题的策略游戏，玩家可以训练海盗军队并征服岛屿。");
//         game3.setLogoUrl("/dist/images/haidaoqibing/haidaoqibing-image.svg");
//         game3.setPosterUrl("/dist/images/haidaoqibing/haidaoqibing-image.svg");
//         game3.setCategories("策略,战争,海洋");
//         game3.setIsFeatured(false);

//         // 保存游戏数据
//         List<Game> games = Arrays.asList(game1, game2, game3);
//         gameRepository.saveAll(games);
//         logger.info("成功初始化 {} 个游戏数据", games.size());
//     }
// }