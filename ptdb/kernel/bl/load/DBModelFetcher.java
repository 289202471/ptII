package ptdb.kernel.bl.load;

import ptdb.common.dto.GetModelsTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// DBModelFetcher

/**
 * This is the business layer that interfaces with the database for retrieving
 * models.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */

public class DBModelFetcher {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a model name representing the model to load, return
     *  an XMLDBModel object that contains the MoML.
     *
     * @param name
     *          The name of the model to be loaded.
     *          
     * @return An XMLDBModel object from the Database,
     *         containing its MoML string.
     *         
     * @exception DBConnectionException
     *          Thrown if there a problem with the database connection.
     *          
     * @exception DBExecutionException
     *          Thrown if there is a problem executing the database task.
     *          
     */
    public static XMLDBModel load(String name) throws DBConnectionException,
            DBExecutionException {

        XMLDBModel returnModel = null;

        DBConnection connection = DBConnectorFactory.getSyncConnection(false);

        try {

            GetModelsTask getModelsTask = new GetModelsTask(name);
            returnModel = connection.executeGetModelsTask(getModelsTask);

        } catch (DBExecutionException dbEx) {            
            throw dbEx;
        } finally {
            if (connection != null) {
                connection.closeConnection();
            }
        }
        return returnModel;
    }
}
