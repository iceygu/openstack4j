package org.openstack4j.openstack.storage.block.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.openstack4j.api.Apis;
import org.openstack4j.api.Builders;
import org.openstack4j.api.storage.BlockVolumeService;
import org.openstack4j.api.storage.BlockVolumeTransferService;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.storage.block.Volume;
import org.openstack4j.model.storage.block.VolumeType;
import org.openstack4j.model.storage.block.VolumeUploadImage;
import org.openstack4j.model.storage.block.options.UploadImageData;
import org.openstack4j.openstack.storage.block.domain.CinderUploadImageData;
import org.openstack4j.openstack.storage.block.domain.CinderVolume;
import org.openstack4j.openstack.storage.block.domain.CinderVolume.Volumes;
import org.openstack4j.openstack.storage.block.domain.CinderVolumeMigration;
import org.openstack4j.openstack.storage.block.domain.CinderVolumeType.VolumeTypes;
import org.openstack4j.openstack.storage.block.domain.ForceDeleteAction;
import org.openstack4j.openstack.storage.block.domain.CinderVolumeUploadImage;

/**
 * Manages Volumes and Volume Type based operations against Block Storage (Cinder)
 * 
 * @author Jeremy Unruh
 */
public class BlockVolumeServiceImpl extends BaseBlockStorageServices implements BlockVolumeService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends VolumeType> listVolumeTypes() {
        return get(VolumeTypes.class, uri("/types")).execute().getList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends Volume> list() {
        return get(Volumes.class, uri("/volumes/detail")).execute().getList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Volume get(String volumeId) {
        checkNotNull(volumeId);
        return get(CinderVolume.class, uri("/volumes/%s", volumeId)).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionResponse delete(String volumeId) {
        checkNotNull(volumeId);
        return deleteWithResponse(uri("/volumes/%s", volumeId)).execute();
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ActionResponse forceDelete(String volumeId) {
		checkNotNull(volumeId);
		return post(ActionResponse.class, uri("/volumes/%s/action", volumeId))
    		    .entity(new ForceDeleteAction())
    		    .execute();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Volume create(Volume volume) {
		checkNotNull(volume);
		return post(CinderVolume.class, uri("/volumes")).entity(volume).execute();
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionResponse update(String volumeId, String name, String description) {
        checkNotNull(volumeId);
        if (name == null && description == null)
            return ActionResponse.actionFailed("Name and Description are both required", 412);

        return put(ActionResponse.class, uri("/volumes/%s", volumeId))
                .entity(Builders.volume().name(name).description(description).build())
                .execute();
    }

    @Override
    public ActionResponse migrate(String volumeId, String hostService, boolean forceHostCopy) {
        CinderVolumeMigration migration = new CinderVolumeMigration(hostService, forceHostCopy);
        return post(ActionResponse.class, uri("/volumes/%s/action", volumeId))
                .entity(migration)
                .execute();
    }

    @Override
    public VolumeUploadImage uploadToImage(String volumeId, UploadImageData data) {
        checkNotNull(volumeId, "volumeId");
        checkNotNull(data, "UploadImageData");
        
        return post(CinderVolumeUploadImage.class, uri("/volumes/%s/action", volumeId))
                .entity(CinderUploadImageData.create(data))
                .execute();
    }

    @Override
    public BlockVolumeTransferService transfer() {
        return Apis.get(BlockVolumeTransferService.class);
    }

}
