package buttondevteam.lib;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import buttondevteam.lib.db.CData;

@Entity
public class CPlayer implements CData {
	public CPlayer() {
	}

	public CPlayer(UUID chromaid, UUID mcid) {
		id = chromaid;
		this.mcid = mcid;
	}

	@Id
	private UUID id;
	@Column(unique = true)
	private UUID mcid;

	@Override
	public UUID getChromaID() {
		return id;
	}

	public void setChromaID(UUID id) {
		this.id = id;
	}

	public UUID getMinecraftID() {
		return mcid;
	}

	public void setMinecraftID(UUID mcid) {
		this.mcid = mcid;
	}

}
