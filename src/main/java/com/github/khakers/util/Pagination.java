package com.github.khakers.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Pagination {

    private static final Logger logger = LogManager.getLogger();


    /**
     * @param paginationValue The pagination value that might be displayed
     * @param userPage        The current page the user is actually on
     * @param pageCount       The total number of pages
     * @return If the current pagination value should be shown in the pagination element
     */
    public static boolean shouldDisplayPage(int paginationValue, int userPage, int pageCount) {
        if (!needsPaginationCulling(pageCount)) {
            logger.debug("pagination result: 1 on page {} {} {}", paginationValue, userPage, pageCount);
            return true;
        }

        if (paginationValue > userPage - 2 && paginationValue < userPage + 2) {
            logger.debug("pagination result: 2 on page {} {} {}", paginationValue, userPage, pageCount);
            return true;
        }

        if (userPage > pageCount - 4 && paginationValue > pageCount - 5) {
            logger.debug("pagination result: 3 on page {} {} {}", paginationValue, userPage, pageCount);
            return true;
        }

        if (userPage <= 4 && paginationValue < 6) {
            logger.debug("pagination result: 4 on page {} {} {}", paginationValue, userPage, pageCount);
            return true;
        }

        if (userPage > pageCount - 3 && paginationValue > pageCount - 3) {
            logger.debug("pagination result: 5 on page {} {} {}", paginationValue, userPage, pageCount);
            return true;
        }
        return false;
    }

    public static boolean needsPaginationCulling(int pageCount) {
        return pageCount > 6;
    }

}
