# --------------------------------------------------------
# Host:                         127.0.0.1
# Server version:               5.2.7-MariaDB
# Server OS:                    Win64
# HeidiSQL version:             6.0.0.3603
# Date/time:                    2011-07-23 17:48:20
# --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

# Dumping database structure for ift
CREATE DATABASE IF NOT EXISTS `ift` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `ift`;


# Dumping structure for table ift.methods
CREATE TABLE IF NOT EXISTS `methods` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `methodKey` varchar(255) DEFAULT NULL,
  `methodName` varchar(100) DEFAULT NULL,
  `path` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `methodKey` (`methodKey`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for table ift.methods_words
CREATE TABLE IF NOT EXISTS `methods_words` (
  `methodID` int(10) unsigned DEFAULT NULL,
  `wordID` int(10) unsigned DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for procedure ift.sp_getAllMethodIdsAndKeys
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getAllMethodIdsAndKeys`()
BEGIN
  SELECT id, methodKey 
  FROM ift.methods;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getAllWordIdsAndWords
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getAllWordIdsAndWords`()
BEGIN
	SELECT id, word
	FROM ift.words;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getIdfDenominator
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getIdfDenominator`(IN `wId` INT)
BEGIN
  SELECT count(distinct(methodID))
  FROM methods_words
  WHERE wordID = wId;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getIdfNumerator
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getIdfNumerator`()
BEGIN
  SELECT count(*) 
  FROM ift.methods;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getMethodDataFromMethodKey
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getMethodDataFromMethodKey`(IN `mKey` varCHAR(255))
BEGIN
  SELECT methodName, path 
  FROM ift.methods 
  WHERE methodKey=mKey;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getMethodIdFromMethodKey
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getMethodIdFromMethodKey`(IN `mKey` VARCHAR(255))
    DETERMINISTIC
BEGIN
  SELECT id
    FROM IFT.methods 
   WHERE methodKey=mKey
   LIMIT 1;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getMethodsFromWordId
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getMethodsFromWordId`(IN `wId` INT)
BEGIN

SELECT methods.id, methods.methodKey
  FROM methods_words
    JOIN methods on methods_words.methodId = methods.id
    WHERE methods_words.wordId = wId;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getMostCommonWords
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getMostCommonWords`(IN `mId1` INT, IN `mId2` INT)
BEGIN
CREATE TEMPORARY TABLE m1
select wordId, count(wordId) as wordCount
from methods_words 
where methodId = mId1
group by wordId
order by wordId;

CREATE TEMPORARY TABLE m2
select wordId, count(wordId) as wordCount
from methods_words 
where methodId = mId2
group by wordId
order by wordId;

SELECT words.word, m1.wordCount, m2.wordCount, (m1.wordCount + m2.wordCount) as total
from m1, m2, words
where m1.wordId = m2.wordId
and m1.wordId = words.id
and m1.wordCount > 0 and m2.wordCount > 0
order by total desc, m1.wordCount desc
limit 8;

drop temporary table if exists m1, m2;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getMostCommonWordsPFIS
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getMostCommonWordsPFIS`(IN `mId` INT)
BEGIN
SELECT methods_words.wordId, words.word, count(methods_words.wordId) as wordCount
FROM methods_words 
JOIN words on methods_words.wordId = words.id
WHERE methodId = mId
GROUP BY methods_words.wordId
ORDER BY wordCount DESC
LIMIT 8;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getMostCommonWordsPFIS2
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getMostCommonWordsPFIS2`(IN `mId` INT)
BEGIN
SELECT wordcounts.wordId, words.word, wordcounts.wordCount
FROM wordcounts 
JOIN words on wordcounts.wordId = words.id
WHERE methodId = mId
ORDER BY wordCount DESC
LIMIT 8;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getMostCommonWordsTFIDF2
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getMostCommonWordsTFIDF2`(IN `mId1` INT, IN `mId2` INT)
BEGIN
CREATE TEMPORARY TABLE m1
select wordId, wordCount
from wordcounts
where methodId = mId1;

CREATE TEMPORARY TABLE m2
select wordId, wordCount
from wordcounts 
where methodId = mId2;

SELECT words.word, m1.wordCount, m2.wordCount, (m1.wordCount + m2.wordCount) as total
FROM words
JOIN m1 ON m1.wordId = words.id
JOIN m2 ON m1.wordId = m2.wordId
WHERE m1.wordCount > 0 and m2.wordCount > 0
order by total desc, m1.wordCount desc
limit 8;

drop temporary table if exists m1, m2;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getNumWords
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getNumWords`()
BEGIN
	SELECT count(*) AS wordCount
	FROM words;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getPathFromMethodKey
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getPathFromMethodKey`(IN `mKey` VARCHAR(255))
BEGIN
  SELECT path
  FROM methods
  WHERE methodKey = mKey
  LIMIT 1;	
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getTfDenominator
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getTfDenominator`(IN `mid` INT)
BEGIN
  SELECT count(*) 
  FROM ift.methods_words 
  WHERE methodID = mId;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getWordCountsFromMethodIdAndWordId
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getWordCountsFromMethodIdAndWordId`(IN `mId` INT, IN `wId` INT)
BEGIN
SELECT wordCount from ift.wordCounts
WHERE methodId = mId and wordId = wId;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getWordIdFromWord
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getWordIdFromWord`(IN `mWord` VARCHAR(100))
    DETERMINISTIC
BEGIN
  SELECT id
    FROM IFT.words
	 WHERE word=mWord
	 LIMIT 1;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_getWordsFromMethodId
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_getWordsFromMethodId`(IN `mId` INT)
BEGIN
SELECT words.id, words.word, count(methods_words.methodId) AS wordCount
  FROM methods_words
    JOIN words on methods_words.wordID = words.id
    WHERE methods_words.methodId = mId
  GROUP BY words.id;
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_insertMethod
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_insertMethod`(IN `mKey` varCHAR(255), IN `mName` varChar(100), IN `mPath` varCHAR(1024))
BEGIN
  INSERT INTO 
    IFT.methods 
	   (methodKey, methodName, path) 
	 VALUES 
	   (mKey, mName, mPath);
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_insertMethodToWordMapping
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_insertMethodToWordMapping`(IN `mId` INT, IN `wId` INT)
BEGIN
  INSERT INTO
    IFT.methods_words
      (methodId, wordId) 
    VALUES 
	   (mId, wId);
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_insertWord
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_insertWord`(IN `wWord` VARCHAR(100))
BEGIN
  INSERT INTO 
    IFT.words 
	   (word)
	 VALUES 
	   (wWord);
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_insertWordCountForMethodIdAndWordId
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_insertWordCountForMethodIdAndWordId`(IN `mId` INT, IN `wId` INT, IN `wc` INT)
BEGIN
INSERT INTO wordCounts (methodId, wordId, wordCount)
VALUES (mId, wId, wc);
END//
DELIMITER ;


# Dumping structure for procedure ift.sp_updateWordCountForMethodIdAndWordId
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_updateWordCountForMethodIdAndWordId`(IN `mId` INT, IN `wId` INT, IN `wc` INT)
BEGIN
UPDATE wordCounts 
SET wordCount = wc
WHERE methodId = mId
AND wordId = wId;
END//
DELIMITER ;


# Dumping structure for table ift.wordcounts
CREATE TABLE IF NOT EXISTS `wordcounts` (
  `methodId` int(11) unsigned NOT NULL,
  `wordId` int(11) unsigned NOT NULL,
  `wordCount` int(10) unsigned DEFAULT NULL,
  KEY `FK_wordcounts_methods` (`methodId`),
  KEY `FK_wordcounts_words` (`wordId`),
  CONSTRAINT `FK_wordcounts_words` FOREIGN KEY (`wordId`) REFERENCES `words` (`id`),
  CONSTRAINT `FK_wordcounts_methods` FOREIGN KEY (`methodId`) REFERENCES `methods` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for table ift.words
CREATE TABLE IF NOT EXISTS `words` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `word` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
