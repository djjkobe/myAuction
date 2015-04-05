---drop all tables

drop table OurSysDate cascade constraints;
drop table Customer cascade constraints;
drop table Administrator cascade constraints;
drop table Product cascade constraints;
drop table Bidlog cascade constraints;
drop table Category cascade constraints;
drop table BelongsTo cascade constraints;


---create tables 

create table ourSysDATE
(
  c_date date,
  constraint pk_ourSysDate primary key(c_date)initially deferred deferrable
);

create table Customer 
(
  login varchar2(10),
  password varchar2(10),
  name varchar2(20),
  address varchar2(30),
  email varchar2(20),
  constraint pk_customer primary key (login) initially deferred deferrable
 );

create table Administrator 
(
  login varchar2(10),
  password varchar2(10),
  name varchar2(20),
  address varchar2(30),
  email varchar2(20),
  constraint pk_administrator primary key (login) initially deferred deferrable
);

create table Product
(
  auction_id int,
  name varchar2(20),
  description varchar2(30),
  seller varchar2(10),
  start_date date,
  min_price int,
  number_of_days int,
  status varchar2(15) not null,
  buyer varchar2(10),
  sell_date date,
  amount int,
  constraint pk_product primary key (auction_id) initially deferred deferrable,
  constraint fk_product_seller foreign key (seller) references Customer(login) initially deferred deferrable,
  constraint fk_product_buyer foreign key (buyer) references Customer(login) initially deferred deferrable
);

create table Bidlog
(
  bidsn int,
  auction_id int,
  bidder varchar2(10),
  bid_time date,
  amount int,
  constraint pk_bidlog primary key (bidsn) initially deferred deferrable,
  constraint fk_bidlog_auctionid foreign key (auction_id) references Product(auction_id) initially deferred deferrable,
  constraint fk_bidlog_bidder foreign key (bidder) references Customer(login) initially deferred deferrable
);


create table Category
(
  name varchar2(20),
  parent_category varchar2(20),
  constraint pk_category primary key (name) initially deferred deferrable,
  constraint fk_category foreign key (parent_category) references Category(name) initially deferred deferrable
);


create table BelongsTo
(
  auction_id int,
  category varchar2(20),
  constraint pk_belongsto primary key (auction_id, category) initially deferred deferrable,
  constraint fk_belongsto_auctionid foreign key (auction_id) references Product(auction_id) initially deferred deferrable,
  constraint fk_belongsto_category foreign key (category) references Category(name) initially deferred deferrable
);


drop sequence seq1;
drop sequence seq2;


create sequence seq1 start with 1 increment by 1 nomaxvalue;
create sequence seq2 start with 1 increment by 1 nomaxvalue;

-- auto-increment triggers
CREATE OR REPLACE TRIGGER product_trigger
BEFORE INSERT ON product
FOR EACH ROW
BEGIN
  SELECT seq1.NEXTVAL
  INTO   :new.auction_id
  FROM   dual;
END;
/

CREATE OR REPLACE TRIGGER bidlog_trigger
BEFORE INSERT ON bidlog
FOR EACH ROW
BEGIN
  SELECT seq2.NEXTVAL
  INTO   :new.bidsn
  FROM   dual;
END;
/

insert into administrator values('admin', 'root', 'administrator', '6810 SENSQ', 'admin@1555.com') ;

insert into customer values('user0', 'pwd', 'user0', '6810 SENSQ', 'user0@1555.com');
insert into customer values('user1', 'pwd', 'user1', '6811 SENSQ', 'user1@1555.com');
insert into customer values('user2', 'pwd', 'user2', '6812 SENSQ', 'user2@1555.com');
insert into customer values('user3', 'pwd', 'user3', '6813 SENSQ', 'user3@1555.com');
insert into customer values('user4', 'pwd', 'user4', '6814 SENSQ', 'user4@1555.com');

insert into product values(1, 'Database', 'SQL ER-design', 'user0', to_date('04-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 50, 2, 'sold', 'user2', to_date('06-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 53);
insert into product values(2, '17 inch monitor', '17 inch monitor', 'user0', to_date('06-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 100, 2, 'sold', 'user4', to_date('08-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 110);
insert into product values(3, 'DELL INSPIRON 1100', 'DELL INSPIRON notebook', 'user0', to_date('07-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 500, 7, 'underauction', null, null, null);
insert into product values(4, 'Return of the King', 'fantasy', 'user1', to_date('07-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 40, 2, 'sold', 'user2', to_date('09-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 40);
insert into product values(5, 'The Sorcerer Stone', 'Harry Porter series', 'user1', to_date('08-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 40, 2, 'sold', 'user3', to_date('10-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 40);
insert into product values(6, 'DELL INSPIRON 1100', 'DELL INSPIRON notebook', 'user1', to_date('09-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 200, 1, 'withdrawn', null, null, null);
insert into product values(7, 'Advanced Database', 'SQL Transaction index', 'user1', to_date('10-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 50, 2, 'underauction', null, null, null);
insert into product values(8, 'Another Database', 'SQL ER-design', 'user1', to_date('04-nov-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 50, 2, 'sold', 'user2', to_date('06-nov-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 53);
insert into product values(9, 'The Sorcerer Stone 2', 'Harry Porter series', 'user1', to_date('08-dec-2012/12:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 40, 2, 'underauction', null, null, null);

insert into bidlog values(1, 1, 'user2', to_date('04-dec-2012/08:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 50);
insert into bidlog values(2, 1, 'user3', to_date('04-dec-2012/09:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 53);
insert into bidlog values(3, 1, 'user2', to_date('05-dec-2012/08:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 60);
insert into bidlog values(4, 2, 'user4', to_date('06-dec-2012/08:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 100);
insert into bidlog values(5, 2, 'user2', to_date('07-dec-2012/08:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 110);
insert into bidlog values(6, 2, 'user4', to_date('07-dec-2012/09:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 120);
insert into bidlog values(7, 4, 'user2', to_date('07-dec-2012/08:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 40);
insert into bidlog values(8, 5, 'user3', to_date('09-dec-2012/08:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 40);
insert into bidlog values(9, 7, 'user2', to_date('07-dec-2012/08:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 55);
insert into bidlog values(10, 1, 'user2', to_date('07-dec-2012/08:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 100);
insert into bidlog values(11, 9, 'user3', to_date('09-dec-2012/08:00:00am', 'dd-mm-yyyy/hh:mi:ssam'), 40);

insert into category values('Books', null);
insert into category values('Textbooks', 'Books');
insert into category values('Fiction books', 'Books');
insert into category values('Magazines', 'Books');
insert into category values('Computer Science', 'Textbooks');
insert into category values('Math', 'Textbooks');
insert into category values('Philosophy', 'Textbooks');
insert into category values('Computer Related', null);
insert into category values('Desktop PCs', 'Computer Related');
insert into category values('Laptops', 'Computer Related');
insert into category values('Monitors', 'Computer Related');
insert into category values('Computer books', 'Computer Related');

insert into belongsto values(1, 'Computer Science');
insert into belongsto values(1, 'Computer books');
insert into belongsto values(2, 'Monitors');
insert into belongsto values(3, 'Laptops');
insert into belongsto values(4, 'Fiction books');
insert into belongsto values(5, 'Fiction books');
insert into belongsto values(6, 'Laptops');
insert into belongsto values(7, 'Computer Science');
insert into belongsto values(7, 'Computer books');
insert into belongsto values(8, 'Computer books');

insert into ourSysDATE values(to_date('01-dec-2011/09:00:00am', 'dd-mm-yyyy/hh:mi:ssam'));

commit;




-- (d) Statistics

-- counts the number of products sold in the past x months for a specific category c
create or replace function product_count(months number, cat varchar2)
return number is
  my_count number;
begin
  select count(p.auction_id) into my_count
  from product p join belongsto b on p.auction_id = b.auction_id
  where b.category = cat and p.status = 'sold' and p.sell_date >= add_months((select c_date from ourSysDATE), -1 * months);
  return my_count;
end;
/


-- counts the number of bids a user u has placed in the past x months
create or replace function bid_count(user varchar2, months number)
return number is
  my_count number;
begin
  select count(bidsn) into my_count
  from bidlog
  where bidder = user and bid_time >= add_months((select c_date from ourSysDATE), -1 * months);
  return my_count;
end;
/


-- calculates the total dollar amount a specific user u has spent in the past x months,
create or replace function buying_amount(user varchar2, months number)
return number is
  my_count number;
begin
  select sum(amount) into my_count
  from product
  where status = 'sold' and buyer = user and sell_date >= add_months((select c_date from ourSysDATE), -1 * months)
  group by buyer;
  return my_count;
end;
/





create or replace type vcarray as table of varchar2(20);
/

-- check if category is valid:
-- select count(name) from category where name = ? or parent_category = ?
-- if count(name) == 1, it is a leaf
-- if count(name) == 0, category doesn't exist
-- if count(name) > 1, category exists, but it is not a leaf

create or replace procedure put_product (
  name in varchar2,
  description in varchar2,
  categories in vcarray,
  days in int,
  seller in varchar2,
  min_price in int,
  id out int
) is
  start_date date;
begin
  select c_date into start_date
  from ourSysDATE;

  insert into product values(1, name, description, seller, start_date, min_price, days, 'underauction', null, null, null) returning auction_id into id;

  -- add categories to product
  -- assume categories are valid (checked in Java)
  for i in 1..categories.count loop
    insert into belongsto values(id, categories(i));
  end loop;

  return;
end;
/


-- (d) Bidding on products

-- advance system time by 5 seconds after a new bid is inserted
CREATE OR REPLACE TRIGGER tri_bidTimeUpdate
BEFORE INSERT ON product
FOR EACH ROW
BEGIN
  update ourSysDATE
  set c_date = c_date + 5/86400 ;
END;
/

-- update the amount attribute for a product after a bid is placed on it
CREATE OR REPLACE TRIGGER tri_updateHighBid
AFTER INSERT ON bidlog
FOR EACH ROW
BEGIN
  update product
  set amount = :new.amount where auction_id = :new.auction_id ;
END;
/

-- check the validity of the new bid (surrounded by transaction?)
-- might be able to return a boolean to use w/ java

create or replace function validate_bid(id int, bid int)
return int is
  invalid exception;
  amount int;
  status varchar2(20);
begin
  
  set transaction isolation level serializable name 'bid' ;
  select amount into amount
  from product
  where auction_id = id;

  if bid <= amount then
    raise invalid;
  end if;

  select status into status
  from product
  where auction_id = id;

  -- sanity check
  if status <> 'underauction' then
    raise invalid;
  end if;
  commit ;
  return 1;
exception
  when invalid then
    return 2; --set to 2 so in java there can be a specific error for if the bid is
	--invalid due to it being too low, or on an incorrect product
  when others then
    return 0;
end;
/


-- (b) Create closeAuctions trigger
-- check if an auction has expired and change its status to 'close'
CREATE OR REPLACE TRIGGER closeAuctions
AFTER UPDATE ON ourSysDATE
BEGIN
  update product
  set status = 'closed'
  where status = 'underauction' and (start_date + number_of_days) < (select c_date from ourSysDATE);
END;
/

commit;
purge recyclebin;