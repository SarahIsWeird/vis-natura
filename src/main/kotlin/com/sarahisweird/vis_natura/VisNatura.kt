package com.sarahisweird.vis_natura

import com.sarahisweird.vis_natura.item.ItemComponents
import com.sarahisweird.vis_natura.networking.PacketHandler
import com.sarahisweird.vis_natura.networking.payloads.ChangeSelectedSpellC2SPayload
import com.sarahisweird.vis_natura.spell.SpellRegistry
import com.sarahisweird.vis_natura.spell.impl.FreezeSpell
import com.sarahisweird.vis_natura.spell.impl.SmiteSpell
import com.sarahisweird.vis_natura.spell.impl.VitalizeSpell
import com.sarahisweird.vis_natura.spell.impl.WarpSpell
import com.sarahisweird.vis_natura.vis.VisType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.slf4j.LoggerFactory
import kotlin.jvm.optionals.getOrNull

object VisNatura : ModInitializer {
    const val MOD_ID = "vis_natura"

    private val isDebug = System.getProperty("vis_natura.debug") == "true"
    private val logger = LoggerFactory.getLogger("Vis Natura")

    val spellRegistry = SpellRegistry()

    override fun onInitialize() {
        logger.info("*peek peek*")

        VisType.init()
        VNItems.init()
        VNBlocks.init()
        ItemComponents.init()

        PayloadTypeRegistry.playC2S().register(
            ChangeSelectedSpellC2SPayload.ID,
            ChangeSelectedSpellC2SPayload.CODEC
        )

        ServerPlayNetworking.registerGlobalReceiver(
            ChangeSelectedSpellC2SPayload.ID,
            PacketHandler::receiveChangeSelectedSpellPacket)

        spellRegistry.register(
            FreezeSpell,
            SmiteSpell,
            VitalizeSpell,
            WarpSpell,
        )

        logger.info("*poke poke*")

        if (isDebug) {
            ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, _, success ->
                if (!success) return@register
                validateBiomes(server)
            }

            ServerWorldEvents.LOAD.register { server, _ ->
                validateBiomes(server)
            }
        }
    }

    private fun validateBiomes(server: MinecraftServer) {
        val biomesWithoutTags = mutableSetOf<String>()
        for (world in server.worlds) {
            biomesWithoutTags.addAll(getBiomesWithoutTags(world))
        }

        for (biome in biomesWithoutTags) {
            logger.warn("Biome {} isn't in any biome vis tag!", biome)
        }
    }

    private fun getBiomesWithoutTags(world: World): Set<String> {
        val biomeRegistry = world.registryManager.getOptional(RegistryKeys.BIOME).getOrNull()
            ?: return emptySet()

        val biomesWithoutTags = mutableSetOf<String>()
        biomeRegistry.indexedEntries.forEach { entry ->
            if (VisType.entries.none { type -> entry.isIn(type.generationBiomeTag) }) {
                biomesWithoutTags.add(entry.idAsString)
            }
        }

        return biomesWithoutTags
    }

    fun id(path: String): Identifier =
        Identifier.of(MOD_ID, path)
}
