CREATE INDEX DURATION_SORT ON ICATLOG(DURATION,USER_ID);
CREATE INDEX LOGTIME_ENTITYTYPE_SORT ON ICATLOG(LOGTIME, USER_ID, ENTITYTYPE);
CREATE INDEX DURATION_OPERATION_SORT ON ICATLOG(DURATION,OPERATION);
CREATE INDEX DURATION_OPERATION_LOGTIME ON ICATLOG(OPERATION,LOGTIME, USER_ID);


