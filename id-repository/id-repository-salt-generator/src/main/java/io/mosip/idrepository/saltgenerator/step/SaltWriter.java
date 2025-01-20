package io.mosip.idrepository.saltgenerator.step;

import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import io.mosip.idrepository.saltgenerator.constant.DatabaseType;
import io.mosip.idrepository.saltgenerator.service.DatabaseContextHolder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.idrepository.saltgenerator.entity.IdRepoSaltEntitiesComposite;
import io.mosip.idrepository.saltgenerator.entity.idmap.VidEncryptSaltEntity;
import io.mosip.idrepository.saltgenerator.entity.idmap.VidHashSaltEntity;
import io.mosip.idrepository.saltgenerator.entity.idrepo.IdentityEncryptSaltEntity;
import io.mosip.idrepository.saltgenerator.entity.idrepo.IdentityHashSaltEntity;
import io.mosip.idrepository.saltgenerator.logger.SaltGeneratorLogger;
import io.mosip.idrepository.saltgenerator.repository.idmap.VidEncryptSaltRepository;
import io.mosip.idrepository.saltgenerator.repository.idmap.VidHashSaltRepository;
import io.mosip.idrepository.saltgenerator.repository.idrepo.IdentityEncryptSaltRepository;
import io.mosip.idrepository.saltgenerator.repository.idrepo.IdentityHashSaltRepository;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.batch.item.Chunk;

/**
 * The Class SaltWriter - Class to write salt entities to DB in batch.
 * Implements {@code ItemWriter}.
 *
 * @author Manoj SP
 */
@Component
public class SaltWriter implements ItemWriter<IdRepoSaltEntitiesComposite> {

	Logger mosipLogger = SaltGeneratorLogger.getLogger(SaltWriter.class);

	@Autowired
	private IdentityHashSaltRepository identityHashSaltRepo;

	@Autowired
	private VidHashSaltRepository vidHashSaltRepo;

	@Autowired
	private IdentityEncryptSaltRepository identityEncryptSaltRepo;

	@Autowired
	private VidEncryptSaltRepository vidEncryptSaltRepo;

	@Override
	@Transactional
	public void write(Chunk<? extends IdRepoSaltEntitiesComposite> entitiesCompositeList) throws Exception {
		try {
			// Set database context for identityHashSaltRepo
			DatabaseContextHolder.set(DatabaseType.IDENTITY);
			long identityHashSaltCount = identityHashSaltRepo.countByIdIn(StreamSupport.stream(entitiesCompositeList.spliterator(), true)
					.map(entities -> entities.getIdentityHashSaltEntity().getId()).collect(Collectors.toList()));

			DatabaseContextHolder.set(DatabaseType.VID);
			long vidHashSaltCount = vidHashSaltRepo.countByIdIn(StreamSupport.stream(entitiesCompositeList.spliterator(), true)
					.map(entities -> entities.getVidHashSaltEntity().getId()).collect(Collectors.toList()));

			DatabaseContextHolder.set(DatabaseType.IDENTITY);
			long identityEncryptSaltCount = identityEncryptSaltRepo.countByIdIn(StreamSupport.stream(entitiesCompositeList.spliterator(), true)
					.map(entities -> entities.getIdentityEncryptSaltEntity().getId()).collect(Collectors.toList()));

			DatabaseContextHolder.set(DatabaseType.VID);
			long vidEncryptSaltCount = vidEncryptSaltRepo.countByIdIn(StreamSupport.stream(entitiesCompositeList.spliterator(), true)
					.map(entities -> entities.getVidEncryptSaltEntity().getId()).collect(Collectors.toList()));

			if (identityHashSaltCount == 0L && vidHashSaltCount == 0L && identityEncryptSaltCount == 0L && vidEncryptSaltCount == 0L) {

				DatabaseContextHolder.set(DatabaseType.IDENTITY);
				List<IdentityHashSaltEntity> idRepoHashSaltEntitiesList = StreamSupport.stream(entitiesCompositeList.spliterator(), true)
						.map(IdRepoSaltEntitiesComposite::getIdentityHashSaltEntity).collect(Collectors.toList());
				identityHashSaltRepo.saveAll(idRepoHashSaltEntitiesList);
				mosipLogger.debug("SALT_GENERATOR", "SaltWriter", "IdRepo Hash Salt Entities written", String.valueOf(idRepoHashSaltEntitiesList.size()));

				DatabaseContextHolder.set(DatabaseType.VID);
				List<VidHashSaltEntity> vidHashSaltEntitiesList = StreamSupport.stream(entitiesCompositeList.spliterator(), true)
						.map(IdRepoSaltEntitiesComposite::getVidHashSaltEntity).collect(Collectors.toList());
				vidHashSaltRepo.saveAll(vidHashSaltEntitiesList);
				mosipLogger.debug("SALT_GENERATOR", "SaltWriter", "IdMap Hash Salt Entities written", String.valueOf(vidHashSaltEntitiesList.size()));

				DatabaseContextHolder.set(DatabaseType.IDENTITY);
				List<IdentityEncryptSaltEntity> idRepoEncryptSaltEntitiesList = StreamSupport.stream(entitiesCompositeList.spliterator(), true)
						.map(IdRepoSaltEntitiesComposite::getIdentityEncryptSaltEntity).collect(Collectors.toList());
				identityEncryptSaltRepo.saveAll(idRepoEncryptSaltEntitiesList);
				mosipLogger.debug("SALT_GENERATOR", "SaltWriter", "IdRepo Encrypt Salt Entities written", String.valueOf(idRepoEncryptSaltEntitiesList.size()));

				DatabaseContextHolder.set(DatabaseType.VID);
				List<VidEncryptSaltEntity> vidEncryptSaltEntitiesList = StreamSupport.stream(entitiesCompositeList.spliterator(), true)
						.map(IdRepoSaltEntitiesComposite::getVidEncryptSaltEntity).collect(Collectors.toList());
				vidEncryptSaltRepo.saveAll(vidEncryptSaltEntitiesList);
				mosipLogger.debug("SALT_GENERATOR", "SaltWriter", "IdMap Encrypt Salt Entities written", String.valueOf(vidEncryptSaltEntitiesList.size()));

			} else {
				mosipLogger.error("SALT_GENERATOR", "SaltWriter", "write", "Records already exist in IdRepo/Vid Salt Table");
			}
		} finally {
			// Reset database context after execution
			DatabaseContextHolder.clear();
		}
	}

}
