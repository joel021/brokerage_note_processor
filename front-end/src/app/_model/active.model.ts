import { ACTIVES } from "../_constants/market";
import * as _ from 'lodash';

export class Active {

    activeNameFromDubiusName(dubiusName: string) : string{

        if (_.has(ACTIVES, dubiusName)){
            return dubiusName
        }

        return this.activeNameFromOptionName(dubiusName)
    }

    activeNameFromOptionName(optionName:string): string {
        
        for (const activeName in ACTIVES){
            
            if (optionName.indexOf(activeName.substring(0, activeName.length-1)) > -1 
                || optionName.indexOf(activeName.substring(0, activeName.length-2)) > -1){
                return activeName
            }

        }

        return optionName
    }

    parseNamesToActiveNames(array){

        for(var i = 0; i < array.length; i++){
            array[i][0] = this.activeNameFromDubiusName(array[i][0])
        }
        
    }

}