package com.agaramtech.qualis.basemaster.service.sampletestcomments;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.SampleTestComments;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface provides methods for CRUD operations on 'SampleTestComments' table.
 * It defines services for getting, creating, updating, deleting, and fetching comment types and subtypes.
 */
public interface SampleTestCommentsService {

    /**
     * This service method retrieves all available sample test comments based on user information.
     * @param userInfo [UserInfo] containing details about the logged-in user
     * @return a ResponseEntity holding the list of sample test comments for the user
     * @throws Exception if any error occurs during the fetch operation
     */
    public ResponseEntity<Object> getSampleTestComments(final UserInfo userInfo) throws Exception;

    /**
     * This service method retrieves a specific sample test comment by its ID.
     * @param nunitCode [int] the primary key of the sample test comment to be fetched
     * @param userInfo [UserInfo] containing details about the logged-in user
     * @return a ResponseEntity holding the sample test comment data for the specified ID
     * @throws Exception if any error occurs during the fetch operation
     */
    public ResponseEntity<Object> getActiveSampleTestCommentsById(final int nunitCode, final UserInfo userInfo) throws Exception;

    /**
     * This service method creates a new sample test comment in the database.
     * @param objSampleTestComments [SampleTestComments] object containing the details of the comment to be created
     * @param userInfo [UserInfo] containing details about the logged-in user
     * @return a ResponseEntity holding the status and data of the created sample test comment
     * @throws Exception if any error occurs during the creation process
     */
    public ResponseEntity<Object> createSampleTestComments(final SampleTestComments objSampleTestComments, final UserInfo userInfo) throws Exception;

    /**
     * This service method updates an existing sample test comment in the database.
     * @param objSampleTestComments [SampleTestComments] object containing the updated details of the comment
     * @param userInfo [UserInfo] containing details about the logged-in user
     * @return a ResponseEntity holding the status and data of the updated sample test comment
     * @throws Exception if any error occurs during the update process
     */
    public ResponseEntity<Object> updateSampleTestComments(final SampleTestComments objSampleTestComments, final UserInfo userInfo) throws Exception;

    /**
     * This service method deletes a sample test comment from the database.
     * @param objSampleTestComments [SampleTestComments] object containing the comment to be deleted
     * @param userInfo [UserInfo] containing details about the logged-in user
     * @return a ResponseEntity holding the status and data of the deleted sample test comment
     * @throws Exception if any error occurs during the deletion process
     */
    public ResponseEntity<Object> deleteSampleTestComments(final SampleTestComments objSampleTestComments, final UserInfo userInfo) throws Exception;

    /**
     * This service method retrieves all available comment types.
     * @param userInfo [UserInfo] containing details about the logged-in user
     * @return a ResponseEntity holding the list of available comment types
     * @throws Exception if any error occurs during the fetch operation
     */
    public ResponseEntity<Object> getCommentType(final UserInfo userInfo) throws Exception;

    /**
     * This service method retrieves all available comment subtypes.
     * @param userInfo [UserInfo] containing details about the logged-in user
     * @return a ResponseEntity holding the list of available comment subtypes
     * @throws Exception if any error occurs during the fetch operation
     */
    public ResponseEntity<Object> getCommentSubType(final UserInfo userInfo) throws Exception;
}
