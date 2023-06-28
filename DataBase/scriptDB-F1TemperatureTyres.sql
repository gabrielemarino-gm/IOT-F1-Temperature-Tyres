CREATE DATABASE  IF NOT EXISTS `F1-Temperature-Tyres`;
USE `F1-Temperature-Tyres`;

DROP TABLE IF EXISTS `temperature_on_track`;
CREATE TABLE `temperature_on_track` (
  `id_temperatureontrack` int(11) NOT NULL AUTO_INCREMENT,    
  `temperature` double NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `tyre_position` INT NOT NULL,
  PRIMARY KEY (`id_temperatureontrack`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `temperature_on_warmer`;
CREATE TABLE `temperature_on_warmer` (
  `id_temperatureonwarmer` int(11) NOT NULL AUTO_INCREMENT,    
  `temperature` double NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `tyre_position` INT NOT NULL,
  PRIMARY KEY (`id_temperatureonwarmer`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1; 