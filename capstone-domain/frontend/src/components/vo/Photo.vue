<template>
    <div style="margin: 0 -15px 0 -15px;">
        <v-card-title>
            {{label}}
        </v-card-title>
        <v-card-text v-if="value">
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Id" v-model="value.id"/>
            </div>
            <div v-else>
                Id :  {{value.id }}
            </div>
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Name" v-model="value.name"/>
            </div>
            <div v-else>
                Name :  {{value.name }}
            </div>
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Url" v-model="value.url"/>
            </div>
            <div v-else>
                Url :  {{value.url }}
            </div>
        </v-card-text>
    </div>
</template>

<script>
    export default {
        name:"Photo",
        props: {
            editMode: Boolean,
            value : Object,
            label : String, 
        },
        created(){
            if(!this.value) {
                this.value = {
                    'id': '',
                    'name': '',
                    'url': '',
                };
            }
        },
        watch: {
            value(newVal) {
                this.$emit('input', newVal);
            },
        },
    }
</script>

<style scoped>
</style>