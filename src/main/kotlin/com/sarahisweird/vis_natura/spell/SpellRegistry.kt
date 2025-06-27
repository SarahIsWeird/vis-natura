package com.sarahisweird.vis_natura.spell

import net.minecraft.util.Identifier

class SpellRegistry {
    private val spellIds = mutableListOf<Identifier>()
    private val spells = mutableMapOf<Identifier, Spell>()

    val size: Int
        get() = spells.size

    fun register(spell: Spell) {
        require(!spells.contains(spell.id)) { "The spell id '${spell.id}' is already registered!" }

        spellIds.add(spell.id)
        spells[spell.id] = spell
    }

    fun register(vararg spells: Spell) {
        for (spell in spells) {
            register(spell)
        }
    }

    operator fun get(index: Int): Spell? {
        return spellIds.getOrNull(index)?.let { spells[it] }
    }

    operator fun get(id: Identifier): Spell? {
        return spells[id]
    }

    fun getSpellId(index: Int): Identifier? {
        return spellIds[index]
    }

    fun getSpellIds(): List<Identifier> {
        return spellIds
    }
}
