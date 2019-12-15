# -*- coding: utf-8 -*-
import os
import logging
import xml.etree.ElementTree as ET
from logging.handlers import RotatingFileHandler
import shutil

class UpdateByToken():

	def __init__(self):		
		self.tmpTxtDir = ".\\TmpTxt\\"
		self.tokenDir = ".\\TokenXml\\"
		self.tokenDoneDir = ".\\TokenXmlDone\\"
		self.tokenFailDir = ".\\TokenXmlFail\\"
		self.jarLocation = "moveData-6.7.22-all.jar"
		self.jreLocation =  "java"
		#self.jreLocation =  "D:\\197CMT_ChangeNotify\\jre1.8.0_171\\bin\\java.exe"
		self.log4j2ConfigFile = "config\log4j2.xml"
		
		logger.info("Start to update 195 [cwbhr], [cwbdy], [cwbmn] table..")
		self.updateByToken()
	
		
	def updateByToken(self):
		
		removeFolderRecursive(self.tmpTxtDir)
		
		#create tmp txt, tokenDone, tokenFail folder
		createFolder(self.tmpTxtDir)
		createFolder(self.tokenDir)
		createFolder(self.tokenDoneDir)
		createFolder(self.tokenFailDir)
		
		#fetch token files
		tokenFileList = os.listdir(self.tokenDir)
		
		
		#parse token, retrieve stno, obstime then wirte to txt
		for tokenName in tokenFileList:
			tokenFilePath = self.tokenDir + tokenName
			logger.info("Parse " + tokenFilePath + "...")
			updateList = parseXML(tokenFilePath)
		  
			s = []
			for data in updateList:
				if data[0] == "count":
					s.append("count;")
					s.append(data[1])
					s.append("\n")
					continue
				s.append(data[0])
				s.append(";")
				s.append(data[1])
				s.append("\n")
			txtStr = "" .join(s)

			# remove ".xml" and append ".txt"
			tmpTokenTxtFile = tokenName[:-4] + ".txt"
			
			#write [stno;obstime] to txt file
			with open(self.tmpTxtDir + tmpTokenTxtFile, "a") as the_file:
				the_file.write(txtStr)
			logger.info("Write to " + tmpTokenTxtFile + " finished")

		#execute java
		cmd = (self.jreLocation + " -Dlog4j.configurationFile=" + self.log4j2ConfigFile + " -jar " + self.jarLocation 
			+ " token " + self.tokenDir + " " + self.tokenDoneDir + " " + self.tokenFailDir + " " + self.tmpTxtDir) 
		logger.info("Execute java: " + cmd)
		os.system(cmd)
		
		logger.info("Java execution finished!")
	
		

def parseXML(tokenFilePath):
	tree = ET.parse(tokenFilePath)
	root = tree.getroot()
	updateList = []
	
	for tag in root.findall("./*"):
		if tag.tag == "count":
			count = tag.text    
			updateList.append( ("count", count[7:-7].strip()) )
	
	for tag in root.findall("./info/*"):
		if tag.tag == "StationID":
			stno = tag.text
		elif tag.tag == "ObsTime":
			obstime = tag.text
			updateList.append((stno, obstime))

	return updateList


def removeFolderRecursive(path):
	if os.path.exists(path):
		shutil.rmtree(path)

def createFolder(path):
	if not os.path.exists(path):
		os.makedirs(path)


if __name__ == "__main__":
	
	createFolder("./log/token")
	
	# create logger
	logger = logging.getLogger('python')
	logger.setLevel(logging.DEBUG)
	handler = RotatingFileHandler("./log/token/python.log",
									maxBytes=500*1024,
									backupCount=3)
# 	handler = TimedRotatingFileHandler("./log/token/python.log",
# 										when="d",
# 										interval=1,
# 										backupCount=14)
	formatter = logging.Formatter('%(asctime)s - %(message)s')
	handler.setFormatter(formatter)
	logger.addHandler(handler)
		
	try:
		logger.info("****************")
		logger.info("****************")
		logger.info("****************")
		logger.info("Program starts!")
		UpdateByToken()
		logger.info("Python program finished!")
	except Exception as e:
		logger.exception("Unexpected exception! %s", e)	