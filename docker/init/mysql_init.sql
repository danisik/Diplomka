create database if not exists patents;
use patents;
create table if not exists patents(
	id int not null auto_increment,
	patent_id int not null,
	title varchar(1000) not null,
	patent_date date not null,
	kind varchar(10),
	country varchar(100) not null,	
	primary key(id)
	);
create table if not exists classification(
	id int not null auto_increment,
	id_patent int not null,
	section varchar(10),
	class varchar(10),		
	primary key(id),
	foreign key (id_patent) references patents(id)
	);
create table if not exists languages(
	id int not null auto_increment,
	id_patent int not null,
	language varchar(10),
	primary key(id),
	foreign key (id_patent) references patents(id)
	);
create table if not exists inventors(
	id int not null auto_increment,
	id_patent int not null,
	inventor varchar(10),
	primary key(id),
	foreign key (id_patent) references patents(id)
	);
create table if not exists applicants(
	id int not null auto_increment,
	id_patent int not null,
	applicant varchar(10),
	primary key(id),
	foreign key (id_patent) references patents(id)
	);