package com.decathlon.ara.features.available;

import com.decathlon.ara.features.IFeature;

/**
 * This feature flipping is used to enable or not the optimization of the requests used in the Execution screen.
 * <p>
 * The optimization is made to shorten the response (by trimming the content and the exception) from the server side
 * and by doing the filter of the Execution page from the client to the server.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
public class ExecutionShortenerFeature implements IFeature {

    @Override
    public String getCode() {
        return "execution-shortener";
    }

    @Override
    public String getName() {
        return "Execution Shortener";
    }

    @Override
    public String getDescription() {
        return "Reduce the size of each execution's JSON with a limit in the exceptions and the contents." +
                "\n" +
                "This feature will impact the execution page also because the filters will need more time to get the " +
                "result from the server (due to the fact a second request will be made instead of front filter only).";
    }
}
