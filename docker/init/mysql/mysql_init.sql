create database if not exists patents;
use patents;
create table if not exists patents(
	id int not null auto_increment,
	patent_id varchar(20) not null,
	title varchar(300) not null,
	patent_date date not null,
	kind varchar(1) not null,
	country varchar(2) not null,
    language varchar(2) not null,
	primary key(id),
	unique (patent_id)
	);
create table if not exists classification(
	id int not null auto_increment,
	section varchar(1) not null,
	class varchar(2) not null,		
	subclass varchar(1) not null,
	primary key(id)
	);
create table if not exists patent_classification(
	id int not null auto_increment,
	id_patent int not null,
	id_classification int not null,
	foreign key (id_patent) references patents(id),
	foreign key (id_classification) references classification(id),
	primary key(id)
	);	
create table if not exists inventors(
	id int not null auto_increment,
	id_patent int not null,
	inventor varchar(100) not null,
	primary key(id),
	foreign key (id_patent) references patents(id)
	);
create table if not exists applicants(
	id int not null auto_increment,
	id_patent int not null,
	applicant varchar(100) not null,
	primary key(id),
	foreign key (id_patent) references patents(id)
	);



----- Views ------
-- 1
CREATE VIEW ten_IL_institutes_2015 AS select count(*), 
count(*) * 100.0 / ((select count(*) from inventors 
left outer join patents on inventors.id_patent = patents.id 
where YEAR(patents.patent_date) = 2015 and patents.patent_id like '%IL%') * 1.0) as percentage, 
inventors.inventor from inventors left outer join patents on inventors.id_patent = patents.id 
where YEAR(patents.patent_date) = 2015 and patents.patent_id like '%IL%' 
group by inventors.inventor order by count(*) desc, percentage desc LIMIT 10;

-- 2
CREATE VIEW five_CA_sections_ge_2010 AS select count(*), 
count(*) * 100.0 / ((select count(*) from patent_classification 
left outer join patents on patents.id = patent_classification.id_patent 
where YEAR(patents.patent_date) >= 2010 and patents.patent_id like '%CA%') * 1.0) as percentage, 
classification.section from classification 
left outer join patent_classification on classification.id = patent_classification.id_classification
left outer join patents on patents.id = patent_classification.id_patent
where YEAR(patents.patent_date) >= 2010 and patents.patent_id like '%CA%' 
group by classification.section order by count(*) asc, percentage asc LIMIT 5;

-- 3
CREATE VIEW twenty_ES_classification_2008 AS select count(*), 
count(*) * 100.0 / ((select count(*) from patent_classification 
left outer join patents on patents.id = patent_classification.id_patent 
where YEAR(patents.patent_date) = 2008 and patents.patent_id LIKE '%ES%') * 1.0) as percentage, 
classification.section, classification.class, classification.subclass from classification 
left outer join patent_classification on classification.id = patent_classification.id_classification
left outer join patents on patents.id = patent_classification.id_patent
where YEAR(patents.patent_date) = 2008 and patents.patent_id LIKE '%ES%' 
group by classification.section, classification.class, classification.subclass 
order by count(*) desc, percentage desc LIMIT 20;

-- 4
create view ten_most_applicants as select count(*), 
count(*) * 100.0 / ((select count(*) from applicants) * 1.0) as percentage, applicants.applicant 
from applicants group by applicants.applicant order by count(*) desc, percentage desc LIMIT 10;

-- 5
create view five_languages_2003 as select count(*), count(*) * 100.0 / ((select count(*) from patents 
where patents.language not like '%-%') * 1.0) as percentage, patents.language from patents 
where patents.language not like '%-%' group by patents.language order by count(*) asc, percentage asc LIMIT 5;

-- 6
create view ten_ES_authors_most_kinds as select count(distinct classification.section), 
count(*) * 100.0 / ((select count(*) from inventors
left outer join patents on patents.id = inventors.id_patent
left outer join patent_classification on patents.id = patent_classification.id_patent
left outer join classification on classification.id = patent_classification.id_classification 
where classification.section is not null and patents.country like '%ES%') * 1.0) as percentage, inventors.inventor 
from inventors 
left outer join patents on patents.id = inventors.id_patent
left outer join patent_classification on patents.id = patent_classification.id_patent
left outer join classification on classification.id = patent_classification.id_classification 
where classification.section is not null and patents.country like '%ES%' group by inventors.inventor 
order by count(distinct classification.section) desc, percentage desc LIMIT 10;

-- 7
create view five_most_country_ge_2018 as select count(*), 
count(*) * 100.0 / ((select count(*) from patents 
where YEAR(patents.patent_date) >= 2018) * 1.0) as percentage, patents.country 
from patents where YEAR(patents.patent_date) >= 2018 
group by patents.country order by count(*) desc, percentage desc LIMIT 5;

-- 8
create view most_FR_kind as select count(*), 
count(*) * 100.0 / ((select count(*) from patents 
where patents.patent_id like '%FR%' and patents.kind not like '%-%') * 1.0) as percentage, patents.kind 
from patents where patents.patent_id like '%FR%' and patents.kind not like '%-%' 
group by patents.kind order by count(*) desc, percentage desc LIMIT 1;

-- 9
create view fifteen_UK_author_textile_2013 as select count(*), 
count(*) * 100.0 / ((select count(*) from inventors 
left outer join patents on patents.id = inventors.id_patent
left outer join patent_classification on patents.id = patent_classification.id_patent
left outer join classification on classification.id = patent_classification.id_classification 
where classification.section like '%D%' and patents.patent_id like '%GB%' 
and YEAR(patents.patent_date) = 2013) * 1.0) as percentage, inventors.inventor 
from inventors 
left outer join patents on patents.id = inventors.id_patent
left outer join patent_classification on patents.id = patent_classification.id_patent
left outer join classification on classification.id = patent_classification.id_classification 
where classification.section like '%D%' and patents.patent_id like '%GB%' 
and YEAR(patents.patent_date) = 2013 group by inventors.inventor order by count(*) desc, percentage desc LIMIT 15;
