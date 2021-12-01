START transaction;

alter TABLE communication ALTER CONSTRAINT "FK5t8uwtv8vxpxkji11v5bl338e" DEFERRABLE;

alter TABLE country_deployment ALTER CONSTRAINT "FK7huof3ay98nqnd2qo1tpha9jr" DEFERRABLE;
alter TABLE country_deployment ALTER CONSTRAINT "FKocj9121ua7k7mlefbpv2yn0a7" DEFERRABLE;

alter TABLE error ALTER CONSTRAINT "FK84lin3r56t0fr1ebbfdp7jyco" DEFERRABLE;

alter TABLE executed_scenario ALTER CONSTRAINT "FK9klaki5wrqxytpd8mc0rhme2q" DEFERRABLE;

alter TABLE execution ALTER CONSTRAINT "FK9ima3x6oi0lhaq4y7t6uxpk42" DEFERRABLE;

alter TABLE functionality_coverage ALTER CONSTRAINT "FK9svcjqg009nq3occpvd7sfx5k" DEFERRABLE;
alter TABLE functionality_coverage ALTER CONSTRAINT "FKpcol94n02a8mv94ls64ao6p" DEFERRABLE;

alter TABLE problem ALTER CONSTRAINT "FKmou64hw0xh3mdspog4s9si9gv" DEFERRABLE;
alter TABLE problem ALTER CONSTRAINT "FKpjf9330s0digv54qmyjnx2p8y" DEFERRABLE;

alter TABLE problem_occurrence ALTER CONSTRAINT "FK7lub23f9xbi92jqumgrjts9tk" DEFERRABLE;
alter TABLE problem_occurrence ALTER CONSTRAINT "FKm3twuhvstqk2icoycegrcmu69" DEFERRABLE;

alter TABLE problem_pattern ALTER CONSTRAINT "FK38mpcmm4yrd45j8acvhb4hpbj" DEFERRABLE;
alter TABLE problem_pattern ALTER CONSTRAINT "FKbeunicbf28ol04ycus2ufc306" DEFERRABLE;
alter TABLE problem_pattern ALTER CONSTRAINT "FKs01euft4h2f624wmm2tc2yjc9" DEFERRABLE;

alter TABLE run ALTER CONSTRAINT "FK8smfl4y1xptsycam7l2t0im88" DEFERRABLE;
alter TABLE run ALTER CONSTRAINT "FKi98yyj4el7fl24gpwngrg5462" DEFERRABLE;
alter TABLE run ALTER CONSTRAINT "FKj1gpmjyluw5u1lw3fn1x0r6pr" DEFERRABLE;

alter TABLE scenario ALTER CONSTRAINT "FKt6q9f0vta0y064s3fi4usfd5k" DEFERRABLE;

alter TABLE "type" ALTER CONSTRAINT "FK420i11bbyj0gxc6ukecynerb0" DEFERRABLE;

SET CONSTRAINTS ALL DEFERRED;
delete from communication cascade;
delete from country cascade;
delete from country_deployment cascade; 
delete from cycle_definition cascade;
delete from databasechangelog cascade;
delete from databasechangeloglock cascade;
delete from error cascade;
delete from executed_scenario cascade;
delete from execution cascade;
delete from execution_completion_request cascade;
delete from functionality cascade;
delete from functionality_coverage cascade;
delete from problem cascade;
delete from problem_occurrence cascade;
delete from problem_pattern cascade;
delete from project cascade;
delete from root_cause cascade;
delete from run cascade;
delete from scenario cascade;
delete from setting cascade;
delete from severity cascade;
delete from "source" cascade;
delete from team cascade;
delete from technology_setting cascade;
delete from "type" cascade;

COMMIT;
