
/* 가게 테이블 */
create table bmpc_store(
   store_id      	int             	auto_increment      comment '가게 아이디',
   store_name       varchar(255)      	not null            comment '가게명',
   branch_name      varchar(255)      	default ''          comment '지점명',
   phone         	varchar(30)     	not null       		comment '전화번호',
   addr         	varchar(255)   		not null       		comment '주소',
   addr_detail      varchar(255)   		default ''      	comment '상세주소',
   rating_avg		decimal(2,1)		default 0			comment '평균 별점(예: 4.3)',
   review_count		int					default	0			comment '리뷰수',
   min_price		int					default 0			comment '최소주문금액',
   origin			text				not null			comment '원산지표시',
   notice			text				default ''			comment '공지사항',
   latitude         decimal(10,7)       null                comment '위도',
   longitude        decimal(10,7)       null                comment '경도',
   create_date  	datetime         	default now()       comment '등록일',
   update_date  	datetime         	default now()       comment '수정일',
   del_yn         	char(1)         	default 'N'    		comment '삭제여부: Y,N',
   
   primary key(store_id)
);

/*
ALTER TABLE bmpc_store
ADD COLUMN latitude  DECIMAL(10,7) NULL COMMENT '위도',
ADD COLUMN longitude DECIMAL(10,7) NULL COMMENT '경도';
*/

/* 가게 파일 테이블 */
create table bmpc_store_files(
   sf_id               	int             auto_increment      comment '파일 아이디',
   store_id            	int             not null            comment '가게 아이디',
   file_name            varchar(255)    not null            comment '파일이름',
   stored_name        	varchar(255)    not null            comment '저장파일이름',
   file_path            varchar(255)    not null            comment '파일경로',
   file_thumb_name      varchar(255)    not null            comment '썸네일 파일 이름',
   file_size         	bigint         	not null         	comment '파일 크기(단위: byte)',
   main_yn            	char(1)         default 'N'       	comment '메인 이미지 여부: Y,N',
   create_date        	datetime        default now()       comment '등록일',
   
   primary key(sf_id),
   constraint store_id_fk3 foreign key(store_id) references bmpc_store(store_id) on delete cascade
);

/* 가게 영업시간 테이블 */
create table bmpc_store_hours(
	sh_id               int             auto_increment      comment '영업시간 아이디',
	store_id            int             not null            comment '가게 아이디',
	day_of_week			int				not null			comment '요일 (1=월, 2=화, ... 7=일)',
	open_time			time 			not null			comment '오픈 시간',
	close_time			time			not null			comment '마감 시간',
	close_yn			char(1)			default 'N'			comment '휴무 여부: Y,N',
	
	primary key(sh_id),
	constraint store_id_fk6 foreign key(store_id) references bmpc_store(store_id) on delete cascade
);

/* 카테고리 테이블 */
create table bmpc_categories(
   ca_id        int               	auto_increment      comment '카테고리 아이디',
   ca_name      varchar(100)      	not null            comment '카테고리명',
   
   primary key(ca_id)
);

/* 가게-카테고리 테이블 */
create table bmpc_store_categories(
	store_ca_id		int				auto_increment		comment '가게-카테고리 아이디',
	store_id		int				not null			comment '가게 아이디',
	ca_id			int				not null			comment '카테고리 아이디',
	
	primary key(store_ca_id),
	constraint store_id_fk foreign key(store_id) references bmpc_store(store_id),
	constraint ca_id_fk foreign key(ca_id) references bmpc_categories(ca_id)
);

/* 권한 테이블 */
create table bmpc_user_role(
	role_id			varchar(255) 	not null		comment '권한 아이디',
	role_name		varchar(255)	not null		comment '권한 이름(ADMIN, OWNER, USER)',
	use_yn			char(1)			default 'Y' 	comment '사용여부 Y,N',
	create_date		datetime		default now()	comment '생성일',
	update_date		datetime		default now()	comment '수정일',
	
	primary key(role_id)
);

insert into bmpc_user_role(role_id, role_name) values('ADMIN', '관리자'), ('OWNER', '점주'), ('USER', '사용자');

/* 회원 테이블 */
create table bmpc_users(
   user_id         	varchar(100)   	not null       	comment '아이디',
   passwd         	varchar(255)   	not null       	comment '패스워드',
   user_name      	varchar(50)     not null       	comment '이름',
   birth         	varchar(30)     not null       	comment '생년월일',
   gender         	varchar(30)     not null       	comment '성별',
   phone         	varchar(30)     not null		comment '전화번호',
   email         	varchar(100)   	not null		comment '이메일',
   deposit         	int            	default 0      	comment '보유금',
   balance			int 			default 0		comment '점주 수익금(정산금)',
   user_role      	varchar(50)     default 'USER'  comment '권한',
   use_yn         	char(1)         default 'Y'    	comment '사용여부: Y,N',
   del_yn         	char(1)         default 'N'    	comment '삭제여부: Y,N',
   store_id			int				default null	comment '가게 아이디',
   create_date      datetime      	default now()   comment '생성일',
   update_date      datetime      	default now()   comment '수정일',
   business_no      varchar(20)     default null    comment '사업자등록번호 (점주만 해당)',
   
   primary key(user_id),
   constraint uq_users_phone unique (phone),
   constraint uq_users_email unique (email),
   constraint user_role_fk2 foreign key(user_role) references bmpc_user_role(role_id),
   constraint store_id_fk2 foreign key(store_id) references bmpc_store(store_id)
);

/*
ALTER TABLE bmpc_users
ADD COLUMN business_no VARCHAR(20) DEFAULT NULL COMMENT '사업자등록번호 (점주만 해당)';
*/

insert into bmpc_users (user_id, passwd, user_name, birth, gender, phone, email, user_role)
values('admin', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', 
'관리자', '199901011', '남자', '010-5555-4444', 'admin@gmail.com', 'ADMIN');

insert into bmpc_users (user_id, passwd, user_name, birth, gender, phone, email, user_role)
values('owner01', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', 
'점주', '19981227', '남자', '010-2222-3333', 'owner@gmail.com', 'OWNER');

insert into bmpc_users (user_id, passwd, user_name, birth, gender, phone, email, user_role)
values('user01', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', 
'사용자', '19981227', '여자', '010-2222-3334', 'user@gmail.com', 'USER');

insert into bmpc_users (user_id, passwd, user_name, birth, gender, phone, email, user_role)
values('user02', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', '사용자', '19981227', '여자', '010-3333-3331', 'user02@gmail.com', 'USER'),
('user03', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', '사용자', '19981227', '여자', '010-3333-3332', 'user03@gmail.com', 'USER'),
('user04', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', '사용자', '19981227', '여자', '010-3333-3333', 'user04@gmail.com', 'USER'),
('user05', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', '사용자', '19981227', '여자', '010-3333-3334', 'user05@gmail.com', 'USER'),
('user06', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', '사용자', '19981227', '여자', '010-3333-3335', 'user06@gmail.com', 'USER'),
('user07', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', '사용자', '19981227', '여자', '010-3333-3336', 'user07@gmail.com', 'USER'),
('user08', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', '사용자', '19981227', '여자', '010-3333-3337', 'user08@gmail.com', 'USER'),
('user09', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', '사용자', '19981227', '여자', '010-3333-3338', 'user09@gmail.com', 'USER'),
('user10', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', '사용자', '19981227', '여자', '010-3333-3339', 'user10@gmail.com', 'USER'),
('user11', '$2a$10$8QleDVBEZYfrYrcrM9nRKuWIiqb15STUzGsY5jHRv0i0KGuU.9H5y', '사용자', '19981227', '여자', '010-3333-3330', 'user11@gmail.com', 'USER');

/*
ALTER TABLE bmpc_users
ADD CONSTRAINT uq_users_phone UNIQUE (phone);

ALTER TABLE bmpc_users
ADD CONSTRAINT uq_users_email UNIQUE (email);
*/

/* ==== 메뉴 예시 ====  */
/* store		menu_category		menu			option_group		option  */
/* BBQ 치킨		메인메뉴				황금올리브치킨		사이즈 선택			반마리	*/
/*																		한마리	*/
/*				사이드메뉴				감자튀김			소스 선택				케첩		*/
/*																		갈릭소스  */
/* 메뉴 카테고리 테이블 (1 depth) */
create table bmpc_menu_category(
   menu_ca_id      	int             	auto_increment      comment '메뉴-카테고리 아이디',
   menu_ca_name     varchar(255)      	not null            comment '메뉴-카테고리명',
   display_order    int            		not null         	comment '정렬 순서',
   create_date  	datetime         	default now()       comment '등록일',
   del_yn         	char(1)         	default 'N'    		comment '삭제여부: Y,N',
   store_id			int					not null			comment '가게 아이디',
   
   primary key(menu_ca_id),
   constraint store_id_fk4 foreign key(store_id) references bmpc_store(store_id)
);

/* 메뉴 테이블 (2 depth) */
create table bmpc_menu(
   menu_id      	int             	auto_increment      comment '메뉴 아이디',
   menu_name     	varchar(255)      	not null            comment '메뉴명',
   description		text				default ''			comment '메뉴 설명',
   price    		int            		default 0         	comment '가격',
   create_date  	datetime         	default now()       comment '등록일',
   update_date		datetime			default now()		comment '수정일',
   soldout_yn		char(1)         	default 'N'    		comment '품절여부: Y,N',
   del_yn         	char(1)         	default 'N'    		comment '삭제여부: Y,N',
   menu_ca_id		int					not null			comment '메뉴-카테고리 아이디',
   
   primary key(menu_id),
   constraint menu_ca_id_fk foreign key(menu_ca_id) references bmpc_menu_category(menu_ca_id)
);

/* 메뉴 파일 테이블 */
create table bmpc_menu_files(
   mf_id               	int             auto_increment      comment '파일 아이디',
   menu_id            	int             not null            comment '메뉴 아이디',
   file_name            varchar(255)    not null            comment '파일이름',
   stored_name        	varchar(255)    not null            comment '저장파일이름',
   file_path            varchar(255)    not null            comment '파일경로',
   file_thumb_name      varchar(255)    not null            comment '썸네일 파일 이름',
   file_size         	bigint         	not null         	comment '파일 크기(단위: byte)',
   create_date        	datetime        default now()       comment '등록일',
   
   primary key(mf_id),
   constraint menu_id_fk foreign key(menu_id) references bmpc_menu(menu_id) on delete cascade
);

/* 메뉴 옵션 그룹 테이블 (3 depth) */
create table bmpc_menu_option_group(
	menu_opt_grp_id		int             auto_increment      comment '메뉴 옵션 그룹 아이디',
	menu_id            	int             not null            comment '메뉴 아이디',
	menu_opt_grp_name	varchar(255)    not null            comment '메뉴 옵션 그룹 이름',
	required_yn			char(1)         default 'N'    		comment '필수선택여부: Y,N',
	del_yn         		char(1)         default 'N'    		comment '삭제여부: Y,N',
	min_select			int				default 0			comment '최소 선택 개수',
	max_select			int				default 0			comment '최대 선택 개수',
	display_order    	int            	not null         	comment '정렬 순서',
	
	primary key(menu_opt_grp_id),
   	constraint menu_id_fk2 foreign key(menu_id) references bmpc_menu(menu_id)
);

/* 메뉴 옵션 테이블 (4 depth) */
create table bmpc_menu_option(
	menu_opt_id			int             auto_increment  	comment '메뉴 옵션 아이디',
	menu_opt_grp_id     int             not null            comment '메뉴 옵션 그룹 아이디',
	menu_opt_name		varchar(255)    not null            comment '메뉴 옵션 이름',
	price				int				default 0			comment '가격',
	available_yn		char(1)         default 'Y'    		comment '선택가능여부: Y,N',
   	del_yn         		char(1)         default 'N'    		comment '삭제여부: Y,N',
	max_select			int				default 0			comment '최대 선택 개수',
	display_order    	int            	not null         	comment '정렬 순서',
	
	primary key(menu_opt_id),
   	constraint menu_opt_grp_id_fk foreign key(menu_opt_grp_id) references bmpc_menu_option_group(menu_opt_grp_id)
);


/* 주문 테이블 */
create table bmpc_orders(
   order_id   		int             auto_increment      comment '주문 번호',
   user_id     		varchar(100)   	not null          	comment '주문한 유저아이디',
   store_id      	int            	not null			comment '가게 아이디',
   order_date   	datetime      	default now()      	comment '주문 일시',
   total_price   	int            	not null         	comment '총액',
   status      		varchar(50)     not null         	comment '상태',
   addr         	varchar(255)   	not null       		comment '주소',
   addr_detail      varchar(255)   	default ''      	comment '상세주소',
   
   primary key(order_id),
   constraint user_id_fk foreign key(user_id) references bmpc_users(user_id),
   constraint store_id_fk7 foreign key(store_id) references bmpc_store(store_id)
);

/* 주문 상세 테이블 */
create table bmpc_order_item(
   item_id      int          	auto_increment      comment '주문 항목 아이디',
   order_id   	int            	not null         	comment '주문 아이디',
   menu_id      int             not null          	comment '메뉴 아이디',
   menu_name	varchar(255)	not null			comment '주문 당시 메뉴명',
   menu_price	int				default 0			comment '주문 당시 메뉴 단가',
   quantity   	int            	default 0         	comment '수량',
   total_price  int            	default 0         	comment '총액',
   
   primary key(item_id),
   constraint menu_id_fk3 foreign key(menu_id) references bmpc_menu(menu_id),
   constraint order_id_fk foreign key(order_id) references bmpc_orders(order_id)
);

/* 주문 옵션(선택한 옵션) 테이블 */
create table bmpc_order_item_option (
    order_item_opt_id 	int 			auto_increment      		comment '주문 항목 옵션 아이디',
    item_id 			int 			not null 					comment '주문 항목 아이디',
    menu_opt_id 		int 			not null 					comment '메뉴 옵션 아이디',
    menu_opt_name 		varchar(255) 	not null 					comment '주문 당시 선택한 옵션명',
    menu_opt_price 		int 			default 0 					comment '주문 당시 선택한 옵션 가격',
    quantity 			int				default 0					comment '옵션 수량',
    total_price			int				default 0					comment '옵션 총액',
    
    primary key(order_item_opt_id),
    constraint item_id_fk foreign key (item_id) references bmpc_order_item(item_id),
    constraint menu_opt_id_fk foreign key (menu_opt_id) references bmpc_menu_option(menu_opt_id)
);


/* 장바구니 테이블 */
create table bmpc_basket (
    basket_id 		int 			auto_increment 	comment '장바구니 아이디',
    user_id 		varchar(100) 	not null 		comment '사용자 아이디',
    store_id 		int 			default null 	comment '가게 아이디',
    total_price 	int 			default 0 		comment '장바구니 총액',
    
    primary key (basket_id),
    constraint user_id_fk8 foreign key (user_id) references bmpc_users(user_id),
    constraint store_id_fk8 foreign key (store_id) references bmpc_store(store_id)
);

/* 장바구니 상세 테이블 */
create table bmpc_basket_item (
    basket_item_id 		int 			auto_increment 	comment '장바구니 항목 아이디',
    basket_id 			int 			not null 		comment '장바구니 아이디',
    menu_id 			int 			not null 		comment '메뉴 아이디',
    menu_name 			varchar(255) 	not null 		comment '담은 메뉴명',
    menu_price 			int 			default 0 		comment '메뉴 단가',
    quantity 			int 			default 0 		comment '수량',
    total_price 		int 			default 0 		comment '총액',

    primary key (basket_item_id),
    constraint basket_id_fk foreign key (basket_id) references bmpc_basket(basket_id) on delete cascade,
    constraint menu_id_fk4 foreign key (menu_id) references bmpc_menu(menu_id)
);

/* 장바구니 옵션(선택한 옵션) 테이블 */
create table bmpc_basket_item_option (
    basket_item_opt_id 	int 			auto_increment 		comment '장바구니 옵션 아이디',
    basket_item_id 		int 			not null 			comment '장바구니 항목 아이디',
    menu_opt_id 		int 			not null 			comment '옵션 아이디',
    menu_opt_name 		varchar(255) 	not null 			comment '옵션명',
    menu_opt_price 		int 			default 0 			comment '옵션 가격',
    quantity 			int 			default 0 			comment '옵션 수량',
    total_price 		int 			default 0 			comment '옵션 총액',

    primary key (basket_item_opt_id),
    constraint basket_item_id_fk foreign key (basket_item_id) references bmpc_basket_item(basket_item_id) on delete cascade,
    constraint menu_opt_id_fk2 foreign key (menu_opt_id) references bmpc_menu_option(menu_opt_id)
);

/* 리뷰 테이블 */
create table bmpc_review(
	review_id		int				auto_increment      comment '리뷰 아이디',
	order_id		int				not null unique		comment '주문 아이디',
	store_id		int				not null			comment '가게 아이디',
	user_id     	varchar(100)   	not null          	comment '리뷰 작성자 아이디',	
	rating			int				not null			comment '별점',
	content			text			not null			comment '내용',
	create_date     datetime      	default now()   	comment '등록일',
   	update_date     datetime      	default now()   	comment '수정일',
   	del_yn         	char(1)         default 'N'    		comment '삭제여부: Y,N',
	
   	primary key(review_id),
   	constraint order_id_fk4	foreign key(order_id) references bmpc_orders(order_id),
   	constraint user_id_fk6 foreign key(user_id) references bmpc_users(user_id),
   	constraint store_id_fk5 foreign key(store_id) references bmpc_store(store_id)
);

/* 리뷰 파일 테이블 */
create table bmpc_review_files(
   rf_id               	int             auto_increment      comment '파일 아이디',
   review_id            int             not null            comment '리뷰 아이디',
   file_name            varchar(255)    not null            comment '파일이름',
   stored_name        	varchar(255)    not null            comment '저장파일이름',
   file_path            varchar(255)    not null            comment '파일경로',
   file_thumb_name      varchar(255)    not null            comment '썸네일 파일 이름',
   file_size         	bigint         	not null         	comment '파일 크기(단위: byte)',
   display_order    	int            	not null         	comment '정렬 순서',
   create_date        	datetime        default now()       comment '등록일',
   
   primary key(rf_id),
   constraint review_id_fk foreign key(review_id) references bmpc_review(review_id) on delete cascade
);

/* 리뷰 답변 테이블 */
create table bmpc_review_reply(
	review_reply_id		int				auto_increment      comment '리뷰 답변 아이디',
	review_id			int				not null unique		comment '리뷰 아이디',
	user_id     		varchar(100)   	not null          	comment '리뷰 답변 작성자(점주) 아이디',	
	content				text			not null			comment '내용',
	create_date     	datetime      	default now()   	comment '생성일',
   	update_date     	datetime      	default now()   	comment '수정일',
   	del_yn         		char(1)         default 'N'    		comment '삭제여부: Y,N',
	
   	primary key(review_reply_id),
   	constraint review_id_fk2 foreign key(review_id) references bmpc_review(review_id),
   	constraint user_id_fk7 foreign key(user_id) references bmpc_users(user_id)
);

/* 찜 테이블 */
create table bmpc_favorite_store (
    favorite_id    	int 				auto_increment 		comment '찜 아이디',
    user_id        	varchar(100) 		not null 			comment '사용자 아이디',
    store_id       	int 				not null 			comment '가게 아이디',
    create_date     datetime      		default now()   	comment '생성일',
   	update_date     datetime      		default now()   	comment '수정일',
    primary key (favorite_id),
    unique key unique_user_store (user_id, store_id), /* 한 유저가 같은 가게를 여러 번 찜하지 않도록 함 */
    constraint fk_fav_user foreign key (user_id) references bmpc_users(user_id),
    constraint fk_fav_store foreign key (store_id) references bmpc_store(store_id)
);

/* 검색 로그 테이블 */
create table bmpc_search_log (
	log_id			int					auto_increment		comment '로그 아이디',
	search_text		varchar(255)    	not null            comment '검색어',
	create_date		datetime			default now()		comment '생성일',
	
	primary key (log_id)
);

/* 인기 검색어 요약 테이블 */
create table bmpc_popular_keyword (
	popular_id			int					auto_increment		comment '인기 검색어 요약 아이디',
	keyword 			varchar(255)		not null unique		comment '검색어',
	search_count		int					default 1			comment '검색 횟수',
	last_search_date	datetime			default now()		comment '마지막 검색일',
	
	primary key (popular_id)
);

/* 인기 검색어 통계 테이블 */
create table bmpc_popular_keyword_stats (
	stat_id				int					auto_increment		comment '인기 검색어 통계 아이디',
	keyword				varchar(255)		not null			comment '검색어',
	stat_date			date 				not null			comment '통계일',
	daily_count			int					not null			comment '당일 검색 횟수',
	total_count			int 				not null			comment '전체 검색 횟수',
	rank				int										comment '순위',
	prev_rank			int										comment '전날 순위',
	rank_diff			int										comment '순위 변화',
	
	primary key(stat_id)
);



