require 'rubygems'
require 'nokogiri'

#f = File.open("Monthly_FOIA_XML_Content_Feed_Spanish_12012012.xml","r+")
f = File.open("Monthly_FOIA_XML_Content_Feed_English_12012012.xml","r+")
doc = Nokogiri::XML(f)

articles = []
search = ".//../Row"
(doc/search).each do |n|
	article = []
	topic = ""
	subtopic = ""
	(n/"Item").each_with_index do |i ,index|
		case index
			when 0...2
				article << i.text()
			when 2
				article << i.content
			when  3..4
				article << i.text()
			when 5 
				topic = i.text()
			when 6
				subtopic = i.text()
		end
	end
	#first article is column headers skip
	next if article[0] == "Link to Content"
	#First real article is guaranteed not to be empty
	if article[1].empty?
		if articles[-1][-1].has_key?(topic)
			articles[-1][-1][topic] << subtopic
		else
			articles[-1][-1][topic] = [subtopic]
		end
	else
		article << { topic => [subtopic] }
		articles << article
	end
end
puts "loaded"

transform = Nokogiri::XML::Builder.new do |xml|
xml.root {
	xml.articles {
		articles.each do |item|
			xml.article {
							xml.id_ item[0].partition("=")[2]
							xml.link_ item[0]
							xml.title_ item[1]
							xml.body_ { xml.cdata item[2] }
							xml.rank_ item[3]
							xml.updated_ item[4]
							xml.topics {
								item[5].keys.each do |topic|
									xml.topic {
										xml.name_ topic
										unless item[5][topic].empty?
											unless (item[5][topic].size == 1) && (item[5][topic][0].empty?)
												xml.subtopics {
													item[5][topic].each do |subtopic|
														unless subtopic.empty?
															xml.subtopic_ subtopic
														end
													end
												}
											end
										end
									}
								end
							}
			}
		end
	}
}
end

#File.open('FAQ_ES.xml', 'w') { |f| f.write(transform.to_xml)}
File.open('FAQ_EN.xml', 'w') { |f| f.write(transform.to_xml)}