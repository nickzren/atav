package atav.analysis.base;

import atav.manager.utils.ErrorManager;

/**
 *
 * @author nick
 */
public abstract class AnalysisBase {

    public abstract void initOutput();

    public abstract void doOutput();

    public abstract void closeOutput();

    public abstract void doAfterCloseOutput();

    public abstract void beforeProcessDatabaseData();

    public abstract void afterProcessDatabaseData();

    public void run() {
        try {
            initOutput();

            beforeProcessDatabaseData();

            processDatabaseData();

            afterProcessDatabaseData();

            closeOutput();

            doAfterCloseOutput();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    public void processDatabaseData() throws Exception {
    }
}
