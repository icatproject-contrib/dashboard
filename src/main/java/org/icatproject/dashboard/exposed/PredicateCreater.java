/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.exposed;

//Creates predicate for all RESTFul calls.
import java.util.Date;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.icatproject.dashboard.entity.Download;
import org.icatproject.dashboard.entity.EntityBaseBean;
import org.icatproject.dashboard.entity.EntityCount;
import org.icatproject.dashboard.entity.ICATUser;
import org.icatproject.dashboard.entity.InstrumentMetaData;
import org.icatproject.dashboard.entity.InvestigationMetaData;

public class PredicateCreater {

    /**
     * *
     * Creates a predicate that applies a restriction to gather all downloads
     * between the start and end date and any during those period.
     *
     * @param cb CriteriaBuilder to build the Predicate.
     * @param start Start time of the predicate statement.
     * @param end End time of the predicate statement.
     * @param download
     * @param userJoin
     * @param userName The name of a ICATuser to add to the predicate.
     * @param method The name of a method to add to the predicate.
     * @return a predicate object that contains restrictions to gather all
     * downloads during the start and end date.
     */
    public static Predicate createDownloadPredicate(CriteriaBuilder cb, Date start, Date end, Root<Download> download, Join<Download, ICATUser> userJoin, String userName, String method) {

        Predicate startGreater = cb.greaterThan(download.<Date>get("downloadStart"), start);
        Predicate endLess = cb.lessThan(download.<Date>get("downloadEnd"), end);
        Predicate betweenStart = cb.between(download.<Date>get("downloadStart"), start, end);
        Predicate betweenEnd = cb.between(download.<Date>get("downloadEnd"), start, end);

        Predicate combineBetween = cb.or(betweenStart, betweenEnd);
        Predicate combineGL = cb.and(startGreater, endLess);
        Predicate finalPredicate = cb.or(combineBetween, combineGL);

        if (!("undefined".equals(method)) && !("".equals(method))) {
            Predicate methodPredicate = cb.equal(download.get("method"), method);
            finalPredicate = cb.and(finalPredicate, methodPredicate);
        }

        if (!("undefined".equals(userName)) && !(("").equals(userName))) {
            Predicate userPredicate = cb.equal(userJoin.get("name"), userName);
            finalPredicate = cb.and(finalPredicate, userPredicate);
        }

        return finalPredicate;

    }

    /**
     * *
     * Creates a predicate that applies a restriction to gather all
     * downloadLocations between the start and end date and any during those
     * period.
     *
     * @param cb CriteriaBuilder to build the Predicate.
     * @param start Start time of the predicate statement.
     * @param end End time of the predicate statement.
     * @param userName The name of a ICATuser to add to the predicate.
     * @param method The name of a method to add to the predicate.
     * @return a predicate object that contains restrictions to gather all
     * downloadLocations during the start and end date.
     */
    public static Predicate createDownloadLocationPredicate(CriteriaBuilder cb, Date start, Date end, Join<? extends EntityBaseBean, ? extends EntityBaseBean> downloadLocationJoin, String userName, String method) {

        Predicate datePredicate = createJoinDatePredicate(cb, "downloadStart", "downloadEnd", start, end, downloadLocationJoin);

        if (!("undefined".equals(method)) && !("".equals(method))) {
            Predicate methodPredicate = cb.equal(downloadLocationJoin.get("method"), method);
            datePredicate = cb.and(datePredicate, methodPredicate);
        }

        if (!("undefined".equals(userName)) && !(("").equals(userName))) {
            Join<ICATUser, Download> downloadUserJoin = downloadLocationJoin.join("user");
            Predicate userPredicate = cb.equal(downloadUserJoin.get("name"), userName);
            datePredicate = cb.and(datePredicate, userPredicate);
        }

        return datePredicate;
    }

    /**
     * Creates a set of date predicate for download-geolocation joins.
     *
     * @param cb CriteriaBuilder to build the Predicate.
     * @param dateTypeFrom name of the start Date in the database.
     * @param dateTypeTo name of the end date in the database
     * @param start Start time of the predicate statement.
     * @param end End time of the predicate statement.
     * @param joinEntity the join entity to have the predicate applied to.
     * @return a predicate
     */
    public static Predicate createJoinDatePredicate(CriteriaBuilder cb, String dateTypeFrom, String dateTypeTo, Date start, Date end, Join<? extends EntityBaseBean, ? extends EntityBaseBean> joinEntity) {

        Predicate startGreater = cb.greaterThan(joinEntity.<Date>get(dateTypeFrom), start);
        Predicate endLess = cb.lessThan(joinEntity.<Date>get(dateTypeTo), end);
        Predicate betweenStart = cb.between(joinEntity.<Date>get(dateTypeFrom), start, end);
        Predicate betweenEnd = cb.between(joinEntity.<Date>get(dateTypeTo), start, end);

        Predicate combineBetween = cb.or(betweenStart, betweenEnd);
        Predicate combineGL = cb.and(startGreater, endLess);
        Predicate finalPredicate = cb.or(combineBetween, combineGL);

        return finalPredicate;

    }

    /**
     * Creates a query to search for the amount of entities created in the ICAT
     * between the specified times.
     *
     * @param cb criteria builder
     * @param entityCount that is to be searched.
     * @param start date from.
     * @param end date from.
     * @param entityType the type of entity to look for.
     * @return a query to be executed for the entity count.
     */
    public static Predicate getEntityCountPredicate(CriteriaBuilder cb, Root<EntityCount> entityCount, Date start, Date end, String entityType) {
       
        Predicate entity = cb.equal(entityCount.<Long>get("entityType"), entityType);

        Predicate dateRange = getDatePredicate(cb,entityCount,start,end,"countDate");

        Predicate finalPredicate = cb.and(dateRange, entity);

        return finalPredicate;

    }

    
    /**
     * Creates a predicate between the two specified dates.
     * @param cb to build the predicate.
     * @param entity to predicate against.
     * @param start date from.
     * @param end date from.
     * @param dateType the date type inside the entity.
     * @return a predicate which applies a where between the start and end date.
     */
    public static Predicate getDatePredicate(CriteriaBuilder cb, Root<?> entity, Date start, Date end, String dateType) {
        
        Predicate startGreater = cb.greaterThanOrEqualTo(entity.<Date>get(dateType), start);
        Predicate endLess = cb.lessThanOrEqualTo(entity.<Date>get(dateType), end);

        return cb.and(startGreater, endLess);

    }

    /**
     * Creates a predicate between two dates and for the specified instrument.
     * @param cb to build the predicate.
     * @param instrument entity to be predicate against.
     * @param start date from.
     * @param end date from.
     * @param instrumentId id of the instrument to search for
     * @param dateType the date type inside the entity.
     * @return a predicate which applies a where between the start, end date and the instrument id.
     */
    public static Predicate getInstrumentPredicate(CriteriaBuilder cb, Root<InstrumentMetaData> instrument, Date start, Date end, String instrumentId, String dateType) {

        
        Predicate instrumentName = cb.equal(instrument.<Long>get("instrumentId"), instrumentId);

        Predicate dateRange = getDatePredicate(cb,instrument,start,end,dateType);

        Predicate finalPredicate = cb.and(dateRange, instrumentName);

        return finalPredicate;   
       
    }
    


}
