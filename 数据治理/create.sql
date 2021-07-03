-- Drop table

-- DROP TABLE data_yys.商家;

CREATE TABLE data_yys.商家 (
	id int4 NOT NULL,
	"name" varchar(64) NOT NULL,
	city varchar(64) NOT NULL,
	"date" date NOT NULL,
	sale int4 NOT NULL,
	product_num int4 NOT NULL,
	CONSTRAINT 商家_pk PRIMARY KEY (id)
)
DISTRIBUTED BY (id);

-- Drop table

-- DROP TABLE data_yys.用户;

CREATE TABLE data_yys.用户 (
	id int4 NOT NULL,
	"name" varchar(64) NOT NULL,
	city varchar(64) NOT NULL,
	phone varchar(64) NOT NULL,
	gender int4 NOT NULL,
	"password" varchar(64) NOT NULL,
	email varchar(64) NOT NULL,
	age int4 NOT NULL,
	"month" int4 NOT NULL,
	CONSTRAINT 用户_pk PRIMARY KEY (id)
)
DISTRIBUTED BY (id);

-- Drop table

-- DROP TABLE data_yys.商品;

CREATE TABLE data_yys.商品 (
	id int4 NOT NULL,
	product_name varchar(64) NOT NULL,
	category varchar(64) NOT NULL,
	sale int4 NOT NULL,
	price int4 NOT NULL,
	shop_id int4 NOT NULL,
	CONSTRAINT 商品_pk PRIMARY KEY (id)
)
DISTRIBUTED BY (id);

-- Drop table

-- DROP TABLE data_yys.销售;

CREATE TABLE data_yys.销售 (
	id int4 NOT NULL,
	user_id int4 NOT NULL,
	product_id int4 NOT NULL,
	sum int4 NOT NULL DEFAULT 1,
	"month" int4 NOT NULL,
	CONSTRAINT 销售_pk PRIMARY KEY (id)
)
DISTRIBUTED BY (id);
