create database if not exists patents;
use patents;
create table if not exists patents(
	id int not null auto_increment,
	patent_id varchar(20) not null,
	title varchar(300) not null,
	patent_date date not null,
	kind varchar(1),
	country varchar(2) not null,
    language varchar(2),
	primary key(id),
	unique (patent_id)
	);
create table if not exists classification(
	id int not null auto_increment,
	id_patent int not null,
	section varchar(1),
	class varchar(2),		
	subclass varchar(1),
	primary key(id),
	foreign key (id_patent) references patents(id)
	);
create table if not exists inventors(
	id int not null auto_increment,
	id_patent int not null,
	inventor varchar(100),
	primary key(id),
	foreign key (id_patent) references patents(id)
	);
create table if not exists applicants(
	id int not null auto_increment,
	id_patent int not null,
	applicant varchar(100),
	primary key(id),
	foreign key (id_patent) references patents(id)
	);